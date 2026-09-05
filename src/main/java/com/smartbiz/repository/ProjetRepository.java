package com.smartbiz.repository;

import com.smartbiz.model.Projet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetRepository extends JpaRepository<Projet, Long> {

    java.util.List<Projet> findTop5ByNomContainingIgnoreCase(String nom);
}
