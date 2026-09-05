package com.smartbiz.repository;

import com.smartbiz.model.Conge;
import com.smartbiz.model.StatutConge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CongeRepository extends JpaRepository<Conge, Long> {

    Page<Conge> findByStatutOrderByDateDebutDesc(StatutConge statut, Pageable pageable);

    Page<Conge> findAllByOrderByDateDebutDesc(Pageable pageable);

    long countByStatut(StatutConge statut);
}
