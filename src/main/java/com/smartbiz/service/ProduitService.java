package com.smartbiz.service;

import com.smartbiz.dto.ProduitDto;
import com.smartbiz.exception.ConflitDonneesException;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Categorie;
import com.smartbiz.model.Fournisseur;
import com.smartbiz.model.Produit;
import com.smartbiz.repository.CategorieRepository;
import com.smartbiz.repository.FournisseurRepository;
import com.smartbiz.repository.ProduitRepository;
import com.smartbiz.specification.ProduitSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;
    private final FournisseurRepository fournisseurRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<Produit> rechercher(String recherche, Long categorieId, Long fournisseurId, Pageable pageable) {
        return produitRepository.findAll(
                ProduitSpecification.avecFiltres(recherche, categorieId, fournisseurId), pageable);
    }

    @Transactional(readOnly = true)
    public Produit trouverParId(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
    }

    @Transactional
    public Produit creer(ProduitDto dto) {
        if (produitRepository.existsByReference(dto.getReference())) {
            throw new ConflitDonneesException("Cette reference produit est deja utilisee.");
        }

        Produit produit = new Produit();
        appliquerDto(produit, dto);
        if (dto.getQuantiteStock() != null) {
            produit.setQuantiteStock(dto.getQuantiteStock());
        }

        Produit enregistre = produitRepository.save(produit);
        auditService.enregistrer("CREATION", "Produit", enregistre.getId().toString(),
                "Creation du produit " + enregistre.getNom());
        return enregistre;
    }

    @Transactional
    public Produit modifier(Long id, ProduitDto dto) {
        Produit existant = trouverParId(id);
        appliquerDto(existant, dto);
        // La quantite en stock ne se modifie PAS depuis ce formulaire : elle passe
        // obligatoirement par un mouvement de stock (StockService) pour garder un historique fiable.
        auditService.enregistrer("MODIFICATION", "Produit", id.toString(),
                "Modification du produit " + existant.getNom());
        return existant;
    }

    @Transactional
    public void supprimer(Long id) {
        Produit existant = trouverParId(id);
        produitRepository.delete(existant);
        auditService.enregistrer("SUPPRESSION", "Produit", id.toString(),
                "Suppression du produit " + existant.getNom());
    }

    @Transactional(readOnly = true)
    public List<Produit> stockFaible() {
        return produitRepository.trouverStockFaible();
    }

    @Transactional(readOnly = true)
    public List<Produit> enRupture() {
        return produitRepository.trouverEnRupture();
    }

    @Transactional(readOnly = true)
    public long compterTous() {
        return produitRepository.count();
    }

    private void appliquerDto(Produit produit, ProduitDto dto) {
        produit.setReference(dto.getReference());
        produit.setNom(dto.getNom());
        produit.setDescription(dto.getDescription());
        produit.setPrixAchat(dto.getPrixAchat());
        produit.setPrixVente(dto.getPrixVente());
        produit.setSeuilMinimum(dto.getSeuilMinimum() != null ? dto.getSeuilMinimum() : 5);
        produit.setActif(dto.isActif());

        if (dto.getCategorieId() != null) {
            Categorie categorie = categorieRepository.findById(dto.getCategorieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categorie introuvable : " + dto.getCategorieId()));
            produit.setCategorie(categorie);
        } else {
            produit.setCategorie(null);
        }

        if (dto.getFournisseurId() != null) {
            Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + dto.getFournisseurId()));
            produit.setFournisseur(fournisseur);
        } else {
            produit.setFournisseur(null);
        }
    }
}
