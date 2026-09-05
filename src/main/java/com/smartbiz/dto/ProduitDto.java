package com.smartbiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProduitDto {

    private Long id;

    @NotBlank(message = "La reference est obligatoire")
    private String reference;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String description;

    private Long categorieId;

    private Long fournisseurId;

    @NotNull(message = "Le prix d'achat est obligatoire")
    @PositiveOrZero(message = "Le prix d'achat doit etre positif")
    private BigDecimal prixAchat;

    @NotNull(message = "Le prix de vente est obligatoire")
    @PositiveOrZero(message = "Le prix de vente doit etre positif")
    private BigDecimal prixVente;

    @PositiveOrZero(message = "Le stock initial doit etre positif")
    private Integer quantiteStock;

    @PositiveOrZero(message = "Le seuil minimum doit etre positif")
    private Integer seuilMinimum;

    private boolean actif = true;
}
