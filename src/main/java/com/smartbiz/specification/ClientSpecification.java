package com.smartbiz.specification;

import com.smartbiz.model.Client;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ClientSpecification {

    private ClientSpecification() {
    }

    public static Specification<Client> avecFiltres(String recherche, Boolean actif) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (StringUtils.hasText(recherche)) {
                String motif = "%" + recherche.toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("nom")), motif),
                        cb.like(cb.lower(root.get("email")), motif),
                        cb.like(cb.lower(root.get("ville")), motif)
                ));
            }

            if (actif != null) {
                predicates = cb.and(predicates, cb.equal(root.get("actif"), actif));
            }

            return predicates;
        };
    }
}
