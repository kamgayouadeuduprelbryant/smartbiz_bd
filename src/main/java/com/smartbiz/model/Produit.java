package com.smartbiz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Table(name = "produits", indexes = {
        @Index(name = "idx_produit_reference", columnList = "reference", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @PositiveOrZero
    @Column(name = "prix_achat", precision = 12, scale = 2, nullable = false)
    private BigDecimal prixAchat;

    @PositiveOrZero
    @Column(name = "prix_vente", precision = 12, scale = 2, nullable = false)
    private BigDecimal prixVente;

    @PositiveOrZero
    @Builder.Default
    @Column(name = "quantite_stock", nullable = false)
    private Integer quantiteStock = 0;

    @PositiveOrZero
    @Builder.Default
    @Column(name = "seuil_minimum", nullable = false)
    private Integer seuilMinimum = 5;

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    @Transient
    public boolean isStockFaible() {
        return quantiteStock != null && seuilMinimum != null && quantiteStock <= seuilMinimum && quantiteStock > 0;
    }

    @Transient
    public boolean isRupture() {
        return quantiteStock != null && quantiteStock <= 0;
    }
}
