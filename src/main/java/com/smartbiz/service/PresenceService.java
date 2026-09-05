package com.smartbiz.service;

import com.smartbiz.dto.PresenceDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Employe;
import com.smartbiz.model.Presence;
import com.smartbiz.repository.EmployeRepository;
import com.smartbiz.repository.PresenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final PresenceRepository presenceRepository;
    private final EmployeRepository employeRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<Presence> parDate(LocalDate date, Pageable pageable) {
        return presenceRepository.findByDateOrderByEmployeNomAsc(date, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Presence> listerToutes(Pageable pageable) {
        return presenceRepository.findAllByOrderByDateDesc(pageable);
    }

    @Transactional
    public Presence enregistrer(PresenceDto dto) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employe introuvable : " + dto.getEmployeId()));

        if (presenceRepository.existsByEmployeAndDate(employe, dto.getDate())) {
            throw new ConflitDonneesException("Une presence est deja enregistree pour cet employe a cette date.");
        }

        Presence presence = Presence.builder()
                .employe(employe)
                .date(dto.getDate())
                .statut(dto.getStatut())
                .heureArrivee(dto.getHeureArrivee())
                .heureDepart(dto.getHeureDepart())
                .notes(dto.getNotes())
                .build();

        Presence enregistree = presenceRepository.save(presence);
        auditService.enregistrer("CREATION", "Presence", enregistree.getId().toString(),
                "Presence enregistree pour " + employe.getNom() + " le " + dto.getDate());
        return enregistree;
    }
}
