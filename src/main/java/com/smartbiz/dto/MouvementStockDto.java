package com.smartbiz.dto;

import com.smartbiz.model.TypeMouvementStock;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MouvementStockDto {

    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;

    @NotNull(message = "Le type de mouvement est obligatoire")
    private TypeMouvementStock type;

    @Positive(message = "La quantite doit etre superieure a zero")
    private Integer quantite;

    private String motif;
}
