package com.smartbiz.service;

import com.smartbiz.dto.DashboardStatsDto;
import com.smartbiz.repository.ClientRepository;
import com.smartbiz.repository.DepartementRepository;
import com.smartbiz.repository.EmployeRepository;
import com.smartbiz.repository.FournisseurRepository;
import com.smartbiz.repository.ProduitRepository;
import com.smartbiz.model.StatutCommande;
import com.smartbiz.model.StatutEmploye;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calcule les statistiques reelles du dashboard depuis PostgreSQL.
 * Volontairement non mis en cache : le dashboard est rafraichi via Fetch API
 * (point 23 du cahier des charges) et doit refleter l'etat reel de la base
 * a chaque appel, pas une valeur perimee.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final ClientRepository clientRepository;
    private final FournisseurRepository fournisseurRepository;
    private final ProduitRepository produitRepository;
    private final CommandeService commandeService;
    private final FactureService factureService;
    private final FinanceService financeService;

    @Transactional(readOnly = true)
    public DashboardStatsDto calculerStats() {
        var financeStats = financeService.calculerStats();

        return DashboardStatsDto.builder()
                .nombreEmployesActifs(employeRepository.countByStatut(StatutEmploye.ACTIF))
                .nombreDepartements(departementRepository.count())
                .nombreClients(clientRepository.countByActifTrue())
                .nombreFournisseurs(fournisseurRepository.countByActifTrue())
                .nombreProduits(produitRepository.count())
                .nombreProduitsStockFaible(produitRepository.compterStockFaible())
                .nombreProduitsRupture(produitRepository.compterEnRupture())
                .nombreCommandesEnAttente(commandeService.compterParStatut(StatutCommande.EN_ATTENTE))
                .nombreFacturesImpayees(factureService.compterImpayees())
                .chiffreAffaires(financeStats.getChiffreAffaires())
                .totalDepenses(financeStats.getTotalDepenses())
                .benefice(financeStats.getBenefice())
                .build();
    }
}
