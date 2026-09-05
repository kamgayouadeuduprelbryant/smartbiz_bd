package com.smartbiz.repository;

import com.smartbiz.model.Depense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DepenseRepository extends JpaRepository<Depense, Long> {

    Page<Depense> findAllByOrderByDateDesc(Pageable pageable);

    @Query("select coalesce(sum(d.montant), 0) from Depense d where d.date between :debut and :fin")
    BigDecimal sommeEntre(LocalDate debut, LocalDate fin);

    @Query("select function('date_trunc', 'month', d.date) as mois, sum(d.montant) " +
           "from Depense d where d.date >= :depuis group by function('date_trunc', 'month', d.date) order by mois")
    List<Object[]> sommesMensuellesDepuis(LocalDate depuis);
}
