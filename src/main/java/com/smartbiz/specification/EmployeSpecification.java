package com.smartbiz.specification;

import com.smartbiz.model.Employe;
import com.smartbiz.model.StatutEmploye;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class EmployeSpecification {

    private EmployeSpecification() {
    }

    public static Specification<Employe> avecFiltres(String recherche, Long departementId, StatutEmploye statut) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (StringUtils.hasText(recherche)) {
                String motif = "%" + recherche.toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("nom")), motif),
                        cb.like(cb.lower(root.get("prenom")), motif),
                        cb.like(cb.lower(root.get("matricule")), motif),
                        cb.like(cb.lower(root.get("email")), motif)
                ));
            }

            if (departementId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("departement").get("id"), departementId));
            }

            if (statut != null) {
                predicates = cb.and(predicates, cb.equal(root.get("statut"), statut));
            }

            return predicates;
        };
    }
}
