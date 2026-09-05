package com.smartbiz.repository;

import com.smartbiz.model.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProduitRepository extends JpaRepository<Produit, Long>, JpaSpecificationExecutor<Produit> {

    boolean existsByReference(String reference);

    /**
     * categorie et fournisseur sont charges en meme temps (EntityGraph) pour
     * permettre leur affichage dans produits/liste sans LazyInitializationException,
     * spring.jpa.open-in-view etant desactive. Sans danger avec la pagination
     * car ce sont des relations ManyToOne (pas de produit cartesien).
     */
    @Override
    @EntityGraph(attributePaths = {"categorie", "fournisseur"})
    Page<Produit> findAll(Specification<Produit> spec, Pageable pageable);

    @Query("select p from Produit p where p.quantiteStock <= p.seuilMinimum and p.quantiteStock > 0 order by p.quantiteStock asc")
    List<Produit> trouverStockFaible();

    @Query("select p from Produit p where p.quantiteStock <= 0 order by p.nom asc")
    List<Produit> trouverEnRupture();

    @Query("select count(p) from Produit p where p.quantiteStock <= p.seuilMinimum and p.quantiteStock > 0")
    long compterStockFaible();

    @Query("select count(p) from Produit p where p.quantiteStock <= 0")
    long compterEnRupture();
}