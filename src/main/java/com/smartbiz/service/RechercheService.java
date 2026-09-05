package com.smartbiz.service;

import com.smartbiz.dto.RechercheResultatDto;
import com.smartbiz.repository.ClientRepository;
import com.smartbiz.repository.EmployeRepository;
import com.smartbiz.repository.FactureRepository;
import com.smartbiz.repository.ProduitRepository;
import com.smartbiz.repository.ProjetRepository;
import com.smartbiz.specification.ClientSpecification;
import com.smartbiz.specification.EmployeSpecification;
import com.smartbiz.specification.ProduitSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Recherche globale (point 25 du cahier des charges) : interroge plusieurs
 * entites en parallele et renvoie une liste unifiee de resultats, limitee a
 * quelques elements par categorie pour rester rapide.
 */
@Service
@RequiredArgsConstructor
public class RechercheService {

    private static final int LIMITE_PAR_CATEGORIE = 5;

    private final EmployeRepository employeRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final FactureRepository factureRepository;
    private final ProjetRepository projetRepository;

    @Transactional(readOnly = true)
    public List<RechercheResultatDto> rechercher(String motCle) {
        List<RechercheResultatDto> resultats = new ArrayList<>();
        var page = PageRequest.of(0, LIMITE_PAR_CATEGORIE);

        employeRepository.findAll(EmployeSpecification.avecFiltres(motCle, null, null), page)
                .forEach(e -> resultats.add(new RechercheResultatDto(
                        "Employe", e.getPrenom() + " " + e.getNom() + " (" + e.getMatricule() + ")",
                        "/employes/" + e.getId())));

        clientRepository.findAll(ClientSpecification.avecFiltres(motCle, null), page)
                .forEach(c -> resultats.add(new RechercheResultatDto(
                        "Client", c.getNom(), "/clients/" + c.getId())));

        produitRepository.findAll(ProduitSpecification.avecFiltres(motCle, null, null), page)
                .forEach(p -> resultats.add(new RechercheResultatDto(
                        "Produit", p.getNom() + " (" + p.getReference() + ")", "/produits/" + p.getId() + "/modifier")));

        factureRepository.findTop5ByNumeroContainingIgnoreCase(motCle)
                .forEach(f -> resultats.add(new RechercheResultatDto(
                        "Facture", f.getNumero() + " — " + f.getClient().getNom(), "/factures/" + f.getId())));

        projetRepository.findTop5ByNomContainingIgnoreCase(motCle)
                .forEach(p -> resultats.add(new RechercheResultatDto(
                        "Projet", p.getNom(), "/projets/" + p.getId())));

        return resultats;
    }
}
