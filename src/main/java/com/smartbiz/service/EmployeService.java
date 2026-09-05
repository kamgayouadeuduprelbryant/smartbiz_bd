package com.smartbiz.service;

import com.smartbiz.dto.EmployeDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Departement;
import com.smartbiz.model.Employe;
import com.smartbiz.model.StatutEmploye;
import com.smartbiz.repository.DepartementRepository;
import com.smartbiz.repository.EmployeRepository;
import com.smartbiz.specification.EmployeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<Employe> rechercher(String recherche, Long departementId, StatutEmploye statut, Pageable pageable) {
        return employeRepository.findAll(
                EmployeSpecification.avecFiltres(recherche, departementId, statut), pageable);
    }

    @Transactional(readOnly = true)
    public Employe trouverParId(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe introuvable : " + id));
    }

    @Transactional
    public Employe creer(EmployeDto dto) {
        if (employeRepository.existsByMatricule(dto.getMatricule())) {
            throw new ConflitDonneesException("Ce matricule est deja utilise.");
        }

        Employe employe = new Employe();
        appliquerDto(employe, dto);

        Employe enregistre = employeRepository.save(employe);
        auditService.enregistrer("CREATION", "Employe", enregistre.getId().toString(),
                "Creation de l'employe " + enregistre.getMatricule());
        return enregistre;
    }

    @Transactional
    public Employe modifier(Long id, EmployeDto dto) {
        Employe existant = trouverParId(id);
        appliquerDto(existant, dto);
        auditService.enregistrer("MODIFICATION", "Employe", id.toString(),
                "Modification de l'employe " + existant.getMatricule());
        return existant;
    }

    @Transactional
    public void supprimer(Long id) {
        Employe existant = trouverParId(id);
        employeRepository.delete(existant);
        auditService.enregistrer("SUPPRESSION", "Employe", id.toString(),
                "Suppression de l'employe " + existant.getMatricule());
    }

    @Transactional(readOnly = true)
    public long compterActifs() {
        return employeRepository.countByStatut(StatutEmploye.ACTIF);
    }

    private void appliquerDto(Employe employe, EmployeDto dto) {
        employe.setMatricule(dto.getMatricule());
        employe.setNom(dto.getNom());
        employe.setPrenom(dto.getPrenom());
        employe.setEmail(dto.getEmail());
        employe.setTelephone(dto.getTelephone());
        employe.setPoste(dto.getPoste());
        employe.setSalaire(dto.getSalaire());
        employe.setDateEmbauche(dto.getDateEmbauche());
        employe.setStatut(dto.getStatut() != null ? dto.getStatut() : StatutEmploye.ACTIF);

        if (dto.getDepartementId() != null) {
            Departement departement = departementRepository.findById(dto.getDepartementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Departement introuvable : " + dto.getDepartementId()));
            employe.setDepartement(departement);
        } else {
            employe.setDepartement(null);
        }
    }
}
