package com.smartbiz.repository;

import com.smartbiz.model.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartementRepository extends JpaRepository<Departement, Long> {

    boolean existsByNomIgnoreCase(String nom);
}
