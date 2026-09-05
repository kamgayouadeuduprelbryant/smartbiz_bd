package com.smartbiz.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Journal d'activite : "ADMIN a cree l'employe EMP-0042".
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_date", columnList = "dateAction")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String utilisateurEmail;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 100)
    private String entiteConcernee;

    @Column(length = 100)
    private String entiteId;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateAction;

    @PrePersist
    void prePersist() {
        if (dateAction == null) {
            dateAction = LocalDateTime.now();
        }
    }
}
