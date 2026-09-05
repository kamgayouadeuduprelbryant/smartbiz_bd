package com.smartbiz.notification;

import com.smartbiz.dto.NotificationDto;
import com.smartbiz.model.Notification;
import com.smartbiz.model.TypeNotification;
import com.smartbiz.model.Utilisateur;
import com.smartbiz.repository.NotificationRepository;
import com.smartbiz.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Centralise la creation et la lecture des notifications internes
 * (point 22 du cahier des charges). Les autres services (Commande, Facture,
 * StockService, CongeService, TacheService...) appellent notifier(...) au
 * moment ou un evenement pertinent se produit.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    /** Notification ciblee sur un utilisateur precis. */
    @Transactional
    public void notifier(Utilisateur destinataire, TypeNotification type, String message, String lien) {
        Notification notification = Notification.builder()
                .destinataire(destinataire).type(type).message(message).lien(lien).build();
        notificationRepository.save(notification);
    }

    /** Notification diffusee a tous les utilisateurs (ex: alerte stock faible). */
    @Transactional
    public void notifierTous(TypeNotification type, String message, String lien) {
        Notification notification = Notification.builder()
                .destinataire(null).type(type).message(message).lien(lien).build();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> pourUtilisateurConnecte(Pageable pageable) {
        Utilisateur utilisateur = utilisateurConnecte();
        if (utilisateur == null) {
            return List.of();
        }
        Page<Notification> page = notificationRepository.trouverPourUtilisateur(utilisateur, pageable);
        return page.getContent().stream().map(this::versDto).toList();
    }

    @Transactional(readOnly = true)
    public long compterNonLues() {
        Utilisateur utilisateur = utilisateurConnecte();
        if (utilisateur == null) {
            return 0;
        }
        return notificationRepository.compterNonLuesPourUtilisateur(utilisateur);
    }

    @Transactional
    public void marquerCommeLue(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setLue(true);
            notificationRepository.save(n);
        });
    }

    private Utilisateur utilisateurConnecte() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return utilisateurRepository.findByEmail(auth.getName()).orElse(null);
    }

    private NotificationDto versDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .type(n.getType().name())
                .message(n.getMessage())
                .lien(n.getLien())
                .lue(n.isLue())
                .dateCreation(n.getDateCreation().format(FORMAT))
                .build();
    }
}
