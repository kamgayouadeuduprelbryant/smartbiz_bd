package com.smartbiz.repository;

import com.smartbiz.model.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Long>, JpaSpecificationExecutor<Fournisseur> {

    long countByActifTrue();

    /**
     * A utiliser pour la page de detail : charge la liste complete des
     * produits en une seule requete (fetch join sans danger ici, un seul
     * fournisseur est charge, pas de pagination), afin d'eviter tout
     * LazyInitializationException dans fournisseurs/detail.html.
     */
    @Query("select distinct f from Fournisseur f left join fetch f.produits where f.id = :id")
    Optional<Fournisseur> trouverParIdAvecProduits(@Param("id") Long id);
}