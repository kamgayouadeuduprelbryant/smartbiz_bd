package com.smartbiz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_stock", indexes = {
        @Index(name = "idx_mouvement_date", columnList = "dateMouvement")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeMouvementStock type;

    @Positive
    @Column(nullable = false)
    private Integer quantite;

    @Column(length = 255)
    private String motif;

    @Column(length = 150)
    private String utilisateurEmail;

    @Column(nullable = false)
    private LocalDateTime dateMouvement;

    @PrePersist
    void prePersist() {
        if (dateMouvement == null) {
            dateMouvement = LocalDateTime.now();
        }
    }
}
