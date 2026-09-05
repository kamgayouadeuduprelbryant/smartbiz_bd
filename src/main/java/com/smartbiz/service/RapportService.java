package com.smartbiz.service;

import com.smartbiz.model.*;
import com.smartbiz.repository.*;
import com.smartbiz.util.ExcelExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Genere les exports Excel du module Rapports (point 27 du cahier des
 * charges) : ventes, depenses, stock, employes, commandes, factures.
 */
@Service
@RequiredArgsConstructor
public class RapportService {

    private final EmployeRepository employeRepository;
    private final ProduitRepository produitRepository;
    private final CommandeRepository commandeRepository;
    private final FactureRepository factureRepository;
    private final RevenuRepository revenuRepository;
    private final DepenseRepository depenseRepository;
    private final FournisseurRepository fournisseurRepository;

    @Transactional(readOnly = true)
    public byte[] exporterEmployes() {
        List<String> entetes = List.of("Matricule", "Nom", "Prenom", "Poste", "Departement", "Statut", "Date embauche");
        List<List<Object>> lignes = new ArrayList<>();

        for (Employe e : employeRepository.findAll()) {
            lignes.add(List.of(
                    e.getMatricule(), e.getNom(), e.getPrenom(),
                    e.getPoste() != null ? e.getPoste() : "",
                    e.getDepartement() != null ? e.getDepartement().getNom() : "",
                    e.getStatut().name(),
                    e.getDateEmbauche() != null ? e.getDateEmbauche().toString() : ""
            ));
        }
        return ExcelExporter.exporter("Employes", entetes, lignes);
    }

    @Transactional(readOnly = true)
    public byte[] exporterStock() {
        List<String> entetes = List.of("Reference", "Nom", "Categorie", "Prix vente", "Stock", "Seuil minimum", "Statut");
        List<List<Object>> lignes = new ArrayList<>();

        for (Produit p : produitRepository.findAll()) {
            String statutStock = p.isRupture() ? "RUPTURE" : (p.isStockFaible() ? "STOCK_FAIBLE" : "OK");
            lignes.add(List.of(
                    p.getReference(), p.getNom(),
                    p.getCategorie() != null ? p.getCategorie().getNom() : "",
                    p.getPrixVente(), p.getQuantiteStock(), p.getSeuilMinimum(), statutStock
            ));
        }
        return ExcelExporter.exporter("Stock", entetes, lignes);
    }

    @Transactional(readOnly = true)
    public byte[] exporterCommandes() {
        List<String> entetes = List.of("Numero", "Client", "Date", "Statut", "Total");
        List<List<Object>> lignes = new ArrayList<>();

        for (Commande c : commandeRepository.findAllByOrderByDateCommandeDesc(PageRequest.of(0, 5000)).getContent()) {
            lignes.add(List.of(c.getNumero(), c.getClient().getNom(), c.getDateCommande().toString(),
                    c.getStatut().name(), c.getTotal()));
        }
        return ExcelExporter.exporter("Commandes", entetes, lignes);
    }

    @Transactional(readOnly = true)
    public byte[] exporterFactures() {
        List<String> entetes = List.of("Numero", "Client", "Emission", "Statut", "Total", "Paye", "Reste a payer");
        List<List<Object>> lignes = new ArrayList<>();

        for (Facture f : factureRepository.findAllByOrderByDateEmissionDesc(PageRequest.of(0, 5000)).getContent()) {
            lignes.add(List.of(f.getNumero(), f.getClient().getNom(), f.getDateEmission().toString(),
                    f.getStatut().name(), f.getTotal(), f.getMontantPaye(), f.getResteAPayer()));
        }
        return ExcelExporter.exporter("Factures", entetes, lignes);
    }

    @Transactional(readOnly = true)
    public byte[] exporterFournisseurs() {
        List<String> entetes = List.of("Nom", "Email", "Telephone", "Ville", "Nombre de produits", "Statut");
        List<List<Object>> lignes = new ArrayList<>();

        for (Fournisseur f : fournisseurRepository.findAll()) {
            lignes.add(List.of(
                    f.getNom(), f.getEmail() != null ? f.getEmail() : "",
                    f.getTelephone() != null ? f.getTelephone() : "",
                    f.getVille() != null ? f.getVille() : "",
                    f.getProduits().size(),
                    f.isActif() ? "ACTIF" : "INACTIF"
            ));
        }
        return ExcelExporter.exporter("Fournisseurs", entetes, lignes);
    }

    @Transactional(readOnly = true)
    public byte[] exporterFinances() {
        List<String> entetes = List.of("Type", "Libelle", "Categorie", "Date", "Montant");
        List<List<Object>> lignes = new ArrayList<>();

        for (Revenu r : revenuRepository.findAllByOrderByDateDesc(PageRequest.of(0, 5000, Sort.by("date").descending())).getContent()) {
            lignes.add(List.of("Revenu", r.getLibelle(), r.getCategorie().name(), r.getDate().toString(), r.getMontant()));
        }
        for (Depense d : depenseRepository.findAllByOrderByDateDesc(PageRequest.of(0, 5000, Sort.by("date").descending())).getContent()) {
            lignes.add(List.of("Depense", d.getLibelle(), d.getCategorie().name(), d.getDate().toString(), d.getMontant()));
        }
        return ExcelExporter.exporter("Finances", entetes, lignes);
    }
}
