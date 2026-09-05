package com.smartbiz.repository;

import com.smartbiz.model.Facture;
import com.smartbiz.model.StatutFacture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    @EntityGraph(attributePaths = "lignes")
    Page<Facture> findByStatutOrderByDateEmissionDesc(StatutFacture statut, Pageable pageable);

    @EntityGraph(attributePaths = "lignes")
    Page<Facture> findAllByOrderByDateEmissionDesc(Pageable pageable);

    long countByStatutIn(List<StatutFacture> statuts);

    @Query("select distinct f from Facture f left join fetch f.lignes where f.statut in :statuts")
    List<Facture> trouverAvecLignesParStatuts(List<StatutFacture> statuts);

    List<Facture> findTop5ByNumeroContainingIgnoreCase(String numero);
}