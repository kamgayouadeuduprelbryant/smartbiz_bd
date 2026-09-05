package com.smartbiz.service;

import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Departement;
import com.smartbiz.repository.DepartementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartementService {

    private final DepartementRepository departementRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Departement> listerTous() {
        return departementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Departement trouverParId(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departement introuvable : " + id));
    }

    @Transactional
    public Departement creer(Departement departement) {
        if (departementRepository.existsByNomIgnoreCase(departement.getNom())) {
            throw new ConflitDonneesException("Un departement porte deja ce nom.");
        }
        Departement enregistre = departementRepository.save(departement);
        auditService.enregistrer("CREATION", "Departement", enregistre.getId().toString(),
                "Creation du departement " + enregistre.getNom());
        return enregistre;
    }

    @Transactional
    public Departement modifier(Long id, Departement donnees) {
        Departement existant = trouverParId(id);
        existant.setNom(donnees.getNom());
        existant.setDescription(donnees.getDescription());
        auditService.enregistrer("MODIFICATION", "Departement", id.toString(),
                "Modification du departement " + existant.getNom());
        return existant;
    }

    @Transactional
    public void supprimer(Long id) {
        Departement existant = trouverParId(id);
        departementRepository.delete(existant);
        auditService.enregistrer("SUPPRESSION", "Departement", id.toString(),
                "Suppression du departement " + existant.getNom());
    }
}
