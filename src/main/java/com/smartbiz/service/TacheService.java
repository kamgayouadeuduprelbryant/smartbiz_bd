package com.smartbiz.service;

import com.smartbiz.dto.TacheDto;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Employe;
import com.smartbiz.model.Projet;
import com.smartbiz.model.StatutTache;
import com.smartbiz.model.Tache;
import com.smartbiz.repository.EmployeRepository;
import com.smartbiz.repository.TacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TacheService {

    private final TacheRepository tacheRepository;
    private final EmployeRepository employeRepository;
    private final ProjetService projetService;
    private final AuditService auditService;

    @Transactional
    public Tache creer(Long projetId, TacheDto dto) {
        Projet projet = projetService.trouverParId(projetId);

        Tache tache = Tache.builder()
                .projet(projet)
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .priorite(dto.getPriorite() != null ? dto.getPriorite() : com.smartbiz.model.PrioriteTache.NORMALE)
                .dateLimite(dto.getDateLimite())
                .statut(StatutTache.A_FAIRE)
                .build();

        if (dto.getResponsableId() != null) {
            Employe responsable = employeRepository.findById(dto.getResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employe introuvable : " + dto.getResponsableId()));
            tache.setResponsable(responsable);
        }

        Tache enregistree = tacheRepository.save(tache);
        auditService.enregistrer("CREATION", "Tache", enregistree.getId().toString(),
                "Creation de la tache " + enregistree.getTitre() + " sur le projet " + projet.getNom());
        return enregistree;
    }

    @Transactional(readOnly = true)
    public Tache trouverParId(Long id) {
        return tacheRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tache introuvable : " + id));
    }

    @Transactional
    public Tache changerStatut(Long id, StatutTache statut) {
        Tache tache = trouverParId(id);
        tache.setStatut(statut);
        auditService.enregistrer("MODIFICATION", "Tache", id.toString(),
                "Tache " + tache.getTitre() + " deplacee vers " + statut);
        return tache;
    }

    @Transactional
    public void supprimer(Long id) {
        Tache tache = trouverParId(id);
        tacheRepository.delete(tache);
        auditService.enregistrer("SUPPRESSION", "Tache", id.toString(), "Suppression de la tache " + tache.getTitre());
    }

    @Transactional(readOnly = true)
    public List<Tache> tachesProchesEcheance() {
        return tacheRepository.findByDateLimiteBeforeAndStatutNot(LocalDate.now().plusDays(3), StatutTache.TERMINEE);
    }

    @Transactional(readOnly = true)
    public List<Tache> listerToutes() {
        return tacheRepository.findAllByOrderByDateLimiteAsc();
    }
}
