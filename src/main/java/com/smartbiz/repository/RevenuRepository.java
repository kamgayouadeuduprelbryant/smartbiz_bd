package com.smartbiz.repository;

import com.smartbiz.model.Revenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RevenuRepository extends JpaRepository<Revenu, Long> {

    Page<Revenu> findAllByOrderByDateDesc(Pageable pageable);

    @Query("select coalesce(sum(r.montant), 0) from Revenu r where r.date between :debut and :fin")
    BigDecimal sommeEntre(LocalDate debut, LocalDate fin);

    @Query("select function('date_trunc', 'month', r.date) as mois, sum(r.montant) " +
           "from Revenu r where r.date >= :depuis group by function('date_trunc', 'month', r.date) order by mois")
    List<Object[]> sommesMensuellesDepuis(LocalDate depuis);
}
