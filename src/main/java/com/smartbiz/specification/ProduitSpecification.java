package com.smartbiz.specification;

import com.smartbiz.model.Produit;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ProduitSpecification {

    private ProduitSpecification() {
    }

    public static Specification<Produit> avecFiltres(String recherche, Long categorieId, Long fournisseurId) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (StringUtils.hasText(recherche)) {
                String motif = "%" + recherche.toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("nom")), motif),
                        cb.like(cb.lower(root.get("reference")), motif)
                ));
            }

            if (categorieId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("categorie").get("id"), categorieId));
            }

            if (fournisseurId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("fournisseur").get("id"), fournisseurId));
            }

            return predicates;
        };
    }
}
