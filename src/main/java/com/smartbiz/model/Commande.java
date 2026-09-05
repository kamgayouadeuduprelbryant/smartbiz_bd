package com.smartbiz.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commandes", indexes = {
        @Index(name = "idx_commande_numero", columnList = "numero", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commande extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Genere apres le premier save (voir CommandeService), format CMD-000123. */
    @Column(length = 30, unique = true)
    private String numero;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Builder.Default
    @Column(name = "date_commande", nullable = false)
    private LocalDate dateCommande = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Column(length = 500)
    private String notes;

    /** Empeche de deduire le stock plusieurs fois si le statut LIVREE est atteint deux fois. */
    @Builder.Default
    @Column(nullable = false)
    private boolean stockDeduit = false;

    @Builder.Default
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LigneCommande> lignes = new ArrayList<>();

    @Transient
    public BigDecimal getTotal() {
        return lignes.stream()
                .map(LigneCommande::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
