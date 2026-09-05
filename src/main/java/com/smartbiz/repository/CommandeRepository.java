package com.smartbiz.repository;

import com.smartbiz.model.Commande;
import com.smartbiz.model.StatutCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    @EntityGraph(attributePaths = "lignes")
    Page<Commande> findByStatutOrderByDateCommandeDesc(StatutCommande statut, Pageable pageable);

    @EntityGraph(attributePaths = "lignes")
    Page<Commande> findAllByOrderByDateCommandeDesc(Pageable pageable);

    long countByStatut(StatutCommande statut);
}