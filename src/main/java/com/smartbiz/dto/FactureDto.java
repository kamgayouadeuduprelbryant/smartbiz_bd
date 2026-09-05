package com.smartbiz.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FactureDto {

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    private LocalDate dateEmission;

    private LocalDate dateEcheance;

    @PositiveOrZero(message = "Le taux de taxe doit etre positif")
    private BigDecimal tauxTaxe;

    @PositiveOrZero(message = "La remise doit etre positive")
    private BigDecimal remiseMontant;
}
