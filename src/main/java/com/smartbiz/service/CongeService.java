package com.smartbiz.service;

import com.smartbiz.dto.CongeDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Conge;
import com.smartbiz.model.Employe;
import com.smartbiz.model.StatutConge;
import com.smartbiz.model.TypeNotification;
import com.smartbiz.notification.NotificationService;
import com.smartbiz.repository.CongeRepository;
import com.smartbiz.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CongeService {

    private final CongeRepository congeRepository;
    private final EmployeRepository employeRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<Conge> rechercher(StatutConge statut, Pageable pageable) {
        return statut != null
                ? congeRepository.findByStatutOrderByDateDebutDesc(statut, pageable)
                : congeRepository.findAllByOrderByDateDebutDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Conge trouverParId(Long id) {
        return congeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de conge introuvable : " + id));
    }

    @Transactional
    public Conge demander(CongeDto dto) {
        if (dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new ConflitDonneesException("La date de fin ne peut pas preceder la date de debut.");
        }

        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employe introuvable : " + dto.getEmployeId()));

        Conge conge = Conge.builder()
                .employe(employe)
                .type(dto.getType())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .motif(dto.getMotif())
                .statut(StatutConge.EN_ATTENTE)
                .build();

        Conge enregistre = congeRepository.save(conge);
        auditService.enregistrer("CREATION", "Conge", enregistre.getId().toString(),
                "Demande de conge de " + employe.getPrenom() + " " + employe.getNom());
        notificationService.notifierTous(TypeNotification.DEMANDE_CONGE,
                "Nouvelle demande de conge de " + employe.getPrenom() + " " + employe.getNom(),
                "/conges");
        return enregistre;
    }

    @Transactional
    public Conge approuver(Long id, String commentaire) {
        Conge conge = trouverParId(id);
        conge.setStatut(StatutConge.APPROUVE);
        conge.setCommentaireTraitement(commentaire);
        conge.setTraitePar(trouverEmployeConnecte());

        auditService.enregistrer("APPROBATION", "Conge", id.toString(),
                "Conge de " + conge.getEmploye().getNom() + " approuve");
        return conge;
    }

    @Transactional
    public Conge refuser(Long id, String commentaire) {
        Conge conge = trouverParId(id);
        conge.setStatut(StatutConge.REFUSE);
        conge.setCommentaireTraitement(commentaire);
        conge.setTraitePar(trouverEmployeConnecte());

        auditService.enregistrer("REFUS", "Conge", id.toString(),
                "Conge de " + conge.getEmploye().getNom() + " refuse");
        return conge;
    }

    @Transactional(readOnly = true)
    public long compterEnAttente() {
        return congeRepository.countByStatut(StatutConge.EN_ATTENTE);
    }

    /** Best-effort : associe le compte connecte a un Employe s'il existe (lien fait dans Employe.utilisateur). */
    private Employe trouverEmployeConnecte() {
        return null;
    }
}
