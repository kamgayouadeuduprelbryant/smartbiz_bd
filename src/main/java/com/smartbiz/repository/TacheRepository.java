package com.smartbiz.repository;

import com.smartbiz.model.Projet;
import com.smartbiz.model.StatutTache;
import com.smartbiz.model.Tache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TacheRepository extends JpaRepository<Tache, Long> {

    List<Tache> findByProjetOrderByDateLimiteAsc(Projet projet);

    List<Tache> findByDateLimiteBeforeAndStatutNot(LocalDate date, StatutTache statutExclu);

    List<Tache> findAllByOrderByDateLimiteAsc();
}
