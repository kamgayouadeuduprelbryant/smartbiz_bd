package com.smartbiz.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class FinanceStatsDto {
    private BigDecimal chiffreAffaires;
    private BigDecimal totalDepenses;
    private BigDecimal benefice;
    private List<MontantMensuelDto> revenusMensuels;
    private List<MontantMensuelDto> depensesMensuelles;
}
