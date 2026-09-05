package com.smartbiz.repository;

import com.smartbiz.model.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Long>, JpaSpecificationExecutor<Employe> {

    Optional<Employe> findByMatricule(String matricule);

    long countByStatut(com.smartbiz.model.StatutEmploye statut);

    boolean existsByMatricule(String matricule);
}
