package com.smartbiz.service;

import com.smartbiz.model.AuditLog;
import com.smartbiz.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void enregistrer(String action, String entiteConcernee, String entiteId, String description) {
        String email = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SYSTEME";

        AuditLog log = AuditLog.builder()
                .utilisateurEmail(email)
                .action(action)
                .entiteConcernee(entiteConcernee)
                .entiteId(entiteId)
                .description(description)
                .build();

        auditLogRepository.save(log);
    }

    public Page<AuditLog> historique(Pageable pageable) {
        return auditLogRepository.findAllByOrderByDateActionDesc(pageable);
    }
}
