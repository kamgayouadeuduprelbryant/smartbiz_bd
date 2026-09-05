package com.smartbiz.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_destinataire", columnList = "destinataire_id, lue")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Null = notification visible par tous les utilisateurs (diffusion large, ex: stock faible). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id")
    private Utilisateur destinataire;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 30)
    private TypeNotification type = TypeNotification.AUTRE;

    @Column(nullable = false, length = 255)
    private String message;

    /** URL relative vers laquelle rediriger au clic (ex: /factures/12). */
    @Column(length = 255)
    private String lien;

    @Builder.Default
    @Column(nullable = false)
    private boolean lue = false;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();
}
