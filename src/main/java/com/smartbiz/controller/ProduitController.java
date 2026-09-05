package com.smartbiz.controller;

import com.smartbiz.dto.ProduitDto;
import com.smartbiz.model.Categorie;
import com.smartbiz.service.CategorieService;
import com.smartbiz.service.FournisseurService;
import com.smartbiz.service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;
    private final CategorieService categorieService;
    private final FournisseurService fournisseurService;

    @GetMapping
    public String liste(@RequestParam(required = false) String recherche,
                         @RequestParam(required = false) Long categorieId,
                         @RequestParam(required = false) Long fournisseurId,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int taille,
                         Model model) {

        Page<?> resultats = produitService.rechercher(recherche, categorieId, fournisseurId,
                PageRequest.of(page, taille, Sort.by("nom").ascending()));

        model.addAttribute("produitsPage", resultats);
        model.addAttribute("categories", categorieService.listerToutes());
        model.addAttribute("fournisseurs", fournisseurService.rechercher(null, true, PageRequest.of(0, 500)).getContent());
        model.addAttribute("recherche", recherche);
        model.addAttribute("categorieId", categorieId);
        model.addAttribute("fournisseurId", fournisseurId);
        return "produits/liste";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("produitDto", new ProduitDto());
        chargerListes(model);
        return "produits/formulaire";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute ProduitDto produitDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            chargerListes(model);
            return "produits/formulaire";
        }
        produitService.creer(produitDto);
        return "redirect:/produits";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        var produit = produitService.trouverParId(id);

        ProduitDto dto = new ProduitDto();
        dto.setId(produit.getId());
        dto.setReference(produit.getReference());
        dto.setNom(produit.getNom());
        dto.setDescription(produit.getDescription());
        dto.setCategorieId(produit.getCategorie() != null ? produit.getCategorie().getId() : null);
        dto.setFournisseurId(produit.getFournisseur() != null ? produit.getFournisseur().getId() : null);
        dto.setPrixAchat(produit.getPrixAchat());
        dto.setPrixVente(produit.getPrixVente());
        dto.setQuantiteStock(produit.getQuantiteStock());
        dto.setSeuilMinimum(produit.getSeuilMinimum());
        dto.setActif(produit.isActif());

        model.addAttribute("produitDto", dto);
        chargerListes(model);
        return "produits/formulaire";
    }

    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id, @Valid @ModelAttribute ProduitDto produitDto,
                            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            chargerListes(model);
            return "produits/formulaire";
        }
        produitService.modifier(id, produitDto);
        return "redirect:/produits";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        produitService.supprimer(id);
        return "redirect:/produits";
    }

    // ---------- Gestion rapide des categories (modale sur la page produits) ----------

    @PostMapping("/categories")
    public String creerCategorie(@ModelAttribute Categorie categorie) {
        categorieService.creer(categorie);
        return "redirect:/produits";
    }

    private void chargerListes(Model model) {
        model.addAttribute("categories", categorieService.listerToutes());
        model.addAttribute("fournisseurs", fournisseurService.rechercher(null, true, PageRequest.of(0, 500)).getContent());
    }
}
