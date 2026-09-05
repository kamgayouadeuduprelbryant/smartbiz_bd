package com.smartbiz.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Assistant SmartBiz (point 38 du cahier des charges). Cette premiere
 * version repond a des questions frequentes via des regles metier simples
 * qui interrogent directement les services existants. L'architecture est
 * volontairement decouplee de la couche presentation (une seule methode
 * poserQuestion(String) -> String) pour permettre de brancher plus tard un
 * moteur d'IA externe sans toucher au controller.
 */
@Service
@RequiredArgsConstructor
public class AssistantService {

    private final ProduitService produitService;
    private final FinanceService financeService;
    private final FactureService factureService;
    private final CommandeService commandeService;
    private final CongeService congeService;

    @Transactional(readOnly = true)
    public String poserQuestion(String question) {
        String normalisee = normaliser(question);

        if (contient(normalisee, "rupture")) {
            var produits = produitService.enRupture();
            if (produits.isEmpty()) {
                return "Aucun produit n'est actuellement en rupture de stock.";
            }
            return produits.size() + " produit(s) en rupture de stock : " +
                    produits.stream().map(p -> p.getNom()).reduce((a, b) -> a + ", " + b).orElse("");
        }

        if (contient(normalisee, "stock faible") || (contient(normalisee, "stock") && contient(normalisee, "faible"))) {
            var produits = produitService.stockFaible();
            if (produits.isEmpty()) {
                return "Aucun produit n'est en stock faible actuellement.";
            }
            return produits.size() + " produit(s) en stock faible : " +
                    produits.stream().map(p -> p.getNom()).reduce((a, b) -> a + ", " + b).orElse("");
        }

        if (contient(normalisee, "chiffre d affaires") || contient(normalisee, "ca ")
                || normalisee.trim().equals("ca") || contient(normalisee, "vente")) {
            var stats = financeService.calculerStats();
            return "Le chiffre d'affaires cumule (factures emises/payees) est de " + stats.getChiffreAffaires() + ".";
        }

        if (contient(normalisee, "benefice")) {
            var stats = financeService.calculerStats();
            return "Le benefice actuel (CA - depenses de l'annee) est de " + stats.getBenefice() + ".";
        }

        if (contient(normalisee, "depense")) {
            var stats = financeService.calculerStats();
            return "Le total des depenses enregistrees cette annee est de " + stats.getTotalDepenses() + ".";
        }

        if (contient(normalisee, "facture") && (contient(normalisee, "impaye") || contient(normalisee, "retard"))) {
            long nombre = factureService.compterImpayees();
            return nombre == 0
                    ? "Aucune facture impayee pour le moment."
                    : nombre + " facture(s) sont actuellement impayees ou partiellement payees.";
        }

        if (contient(normalisee, "commande") && contient(normalisee, "attente")) {
            long nombre = commandeService.compterParStatut(com.smartbiz.model.StatutCommande.EN_ATTENTE);
            return nombre == 0
                    ? "Aucune commande en attente."
                    : nombre + " commande(s) sont en attente de traitement.";
        }

        if (contient(normalisee, "conge") && contient(normalisee, "attente")) {
            long nombre = congeService.compterEnAttente();
            return nombre == 0
                    ? "Aucune demande de conge en attente d'approbation."
                    : nombre + " demande(s) de conge sont en attente d'approbation.";
        }

        return "Je ne sais pas encore repondre a cette question. Essayez par exemple : " +
                "\"Quels produits sont en rupture ?\", \"Quel est mon chiffre d'affaires ?\", " +
                "\"Quelles sont mes depenses ?\", \"Combien de factures sont impayees ?\".";
    }

    @Transactional(readOnly = true)
    public List<String> suggestions() {
        return List.of(
                "Quels produits sont bientot en rupture ?",
                "Quel est mon chiffre d'affaires ?",
                "Quelles sont mes depenses les plus importantes ?",
                "Combien de factures sont impayees ?",
                "Combien de conges sont en attente ?"
        );
    }

    private boolean contient(String texte, String motif) {
        return texte.contains(motif);
    }

    private static final Pattern DIACRITIQUES = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private String normaliser(String texte) {
        String sansAccents = Normalizer.normalize(texte, Normalizer.Form.NFD);
        return DIACRITIQUES.matcher(sansAccents).replaceAll("").toLowerCase();
    }
}
