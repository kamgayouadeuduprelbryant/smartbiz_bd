package com.smartbiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LigneFactureDto {

    /** Optionnel : si renseigne, prefiltre le libelle et le prix depuis le catalogue produit. */
    private Long produitId;

    @NotBlank(message = "Le libelle est obligatoire")
    private String libelle;

    @Positive(message = "La quantite doit etre superieure a zero")
    private Integer quantite;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix unitaire doit etre superieur a zero")
    private BigDecimal prixUnitaire;
}
