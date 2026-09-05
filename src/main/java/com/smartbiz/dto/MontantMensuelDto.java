package com.smartbiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MontantMensuelDto {
    private String mois;
    private BigDecimal montant;
}
