package com.smartbiz.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "presences", uniqueConstraints = {
        @UniqueConstraint(name = "uk_presence_employe_date", columnNames = {"employe_id", "date"})
}, indexes = {
        @Index(name = "idx_presence_date", columnList = "date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presence extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Builder.Default
    @Column(nullable = false)
    private LocalDate date = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private StatutPresence statut = StatutPresence.PRESENT;

    private LocalTime heureArrivee;

    private LocalTime heureDepart;

    @Column(length = 255)
    private String notes;
}
