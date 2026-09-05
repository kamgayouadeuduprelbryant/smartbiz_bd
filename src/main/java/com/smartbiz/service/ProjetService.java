package com.smartbiz.service;

import com.smartbiz.dto.ProjetDto;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Employe;
import com.smartbiz.model.Projet;
import com.smartbiz.repository.EmployeRepository;
import com.smartbiz.repository.ProjetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final EmployeRepository employeRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Projet> listerTous() {
        return projetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Projet trouverParId(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable : " + id));
    }

    @Transactional
    public Projet creer(ProjetDto dto) {
        Projet projet = Projet.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                .statut(dto.getStatut() != null ? dto.getStatut() : com.smartbiz.model.StatutProjet.PLANIFIE)
                .dateDebut(dto.getDateDebut())
                .dateEcheance(dto.getDateEcheance())
                .membres(resoudreMembres(dto.getMembreIds()))
                .build();

        Projet enregistre = projetRepository.save(projet);
        auditService.enregistrer("CREATION", "Projet", enregistre.getId().toString(),
                "Creation du projet " + enregistre.getNom());
        return enregistre;
    }

    @Transactional
    public Projet modifier(Long id, ProjetDto dto) {
        Projet existant = trouverParId(id);
        existant.setNom(dto.getNom());
        existant.setDescription(dto.getDescription());
        if (dto.getStatut() != null) {
            existant.setStatut(dto.getStatut());
        }
        existant.setDateDebut(dto.getDateDebut());
        existant.setDateEcheance(dto.getDateEcheance());
        existant.setMembres(resoudreMembres(dto.getMembreIds()));

        auditService.enregistrer("MODIFICATION", "Projet", id.toString(),
                "Modification du projet " + existant.getNom());
        return existant;
    }

    @Transactional
    public void supprimer(Long id) {
        Projet existant = trouverParId(id);
        projetRepository.delete(existant);
        auditService.enregistrer("SUPPRESSION", "Projet", id.toString(),
                "Suppression du projet " + existant.getNom());
    }

    private Set<Employe> resoudreMembres(List<Long> membreIds) {
        if (membreIds == null || membreIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(employeRepository.findAllById(membreIds));
    }
}
