package com.smartbiz.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Entity
@Table(name = "conges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conge extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 30)
    private TypeConge type = TypeConge.CONGE_PAYE;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(length = 500)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private StatutConge statut = StatutConge.EN_ATTENTE;

    /** Employe (responsable/RH) qui a traite la demande. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traite_par_id")
    private Employe traitePar;

    @Column(length = 500)
    private String commentaireTraitement;

    @Transient
    public long getNombreJours() {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
    }
}
