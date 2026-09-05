package com.smartbiz.repository;

import com.smartbiz.model.Employe;
import com.smartbiz.model.Presence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    Page<Presence> findByDateOrderByEmployeNomAsc(LocalDate date, Pageable pageable);

    Page<Presence> findAllByOrderByDateDesc(Pageable pageable);

    Optional<Presence> findByEmployeAndDate(Employe employe, LocalDate date);

    boolean existsByEmployeAndDate(Employe employe, LocalDate date);
}
