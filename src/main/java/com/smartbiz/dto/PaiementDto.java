package com.smartbiz.dto;

import com.smartbiz.model.ModePaiement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaiementDto {

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit etre superieur a zero")
    private BigDecimal montant;

    @NotNull(message = "Le mode de paiement est obligatoire")
    private ModePaiement modePaiement;

    private String reference;
}
