package com.smartbiz.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factures", indexes = {
        @Index(name = "idx_facture_numero", columnList = "numero", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Genere apres le premier save (voir FactureService), format FAC-000123. */
    @Column(length = 30, unique = true)
    private String numero;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /** Facture optionnellement issue d'une commande. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @Builder.Default
    @Column(name = "date_emission", nullable = false)
    private LocalDate dateEmission = LocalDate.now();

    private LocalDate dateEcheance;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 25)
    private StatutFacture statut = StatutFacture.BROUILLON;

    /** Pourcentage de taxe applique au sous-total, ex: 19.25 */
    @Builder.Default
    @Column(name = "taux_taxe", precision = 5, scale = 2, nullable = false)
    private BigDecimal tauxTaxe = BigDecimal.ZERO;

    /** Montant de remise fixe applique au total. */
    @Builder.Default
    @Column(name = "remise_montant", precision = 12, scale = 2, nullable = false)
    private BigDecimal remiseMontant = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "montant_paye", precision = 12, scale = 2, nullable = false)
    private BigDecimal montantPaye = BigDecimal.ZERO;

    @Builder.Default
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LigneFacture> lignes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Paiement> paiements = new ArrayList<>();

    @Transient
    public BigDecimal getSousTotal() {
        return lignes.stream()
                .map(LigneFacture::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getMontantTaxe() {
        return getSousTotal().multiply(tauxTaxe).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getTotal() {
        return getSousTotal().add(getMontantTaxe()).subtract(remiseMontant).max(BigDecimal.ZERO);
    }

    @Transient
    public BigDecimal getResteAPayer() {
        return getTotal().subtract(montantPaye).max(BigDecimal.ZERO);
    }
}
