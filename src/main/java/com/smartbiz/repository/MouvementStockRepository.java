package com.smartbiz.repository;

import com.smartbiz.model.MouvementStock;
import com.smartbiz.model.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    /**
     * produit est charge en meme temps (EntityGraph) pour permettre son
     * affichage dans stock/liste sans LazyInitializationException,
     * spring.jpa.open-in-view etant desactive. Sans danger avec la pagination
     * car produit est une relation ManyToOne (pas de produit cartesien).
     */
    @EntityGraph(attributePaths = "produit")
    Page<MouvementStock> findAllByOrderByDateMouvementDesc(Pageable pageable);

    @EntityGraph(attributePaths = "produit")
    Page<MouvementStock> findByProduitOrderByDateMouvementDesc(Produit produit, Pageable pageable);
}