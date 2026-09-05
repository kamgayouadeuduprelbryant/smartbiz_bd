package com.smartbiz.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LigneCommandeDto {

    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;

    @Positive(message = "La quantite doit etre superieure a zero")
    private Integer quantite;
}
