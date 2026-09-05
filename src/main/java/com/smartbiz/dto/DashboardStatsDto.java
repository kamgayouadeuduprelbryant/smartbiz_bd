package com.smartbiz.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardStatsDto {
    private long nombreEmployesActifs;
    private long nombreDepartements;
    private long nombreClients;
    private long nombreFournisseurs;
    private long nombreProduits;
    private long nombreProduitsStockFaible;
    private long nombreProduitsRupture;
    private long nombreCommandesEnAttente;
    private long nombreFacturesImpayees;
    private BigDecimal chiffreAffaires;
    private BigDecimal totalDepenses;
    private BigDecimal benefice;
}
