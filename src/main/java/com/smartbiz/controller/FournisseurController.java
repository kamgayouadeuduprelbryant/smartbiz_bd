package com.smartbiz.controller;

import com.smartbiz.model.Fournisseur;
import com.smartbiz.service.FournisseurService;
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
@RequestMapping("/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @GetMapping
    public String liste(@RequestParam(required = false) String recherche,
                        @RequestParam(required = false) Boolean actif,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int taille,
                        Model model) {

        Page<Fournisseur> resultats = fournisseurService.rechercher(recherche, actif,
                PageRequest.of(page, taille, Sort.by("nom").ascending()));

        model.addAttribute("fournisseursPage", resultats);
        model.addAttribute("recherche", recherche);
        model.addAttribute("actif", actif);
        return "fournisseurs/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("fournisseur", fournisseurService.trouverParIdAvecProduits(id));
        return "fournisseurs/detail";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("fournisseur", new Fournisseur());
        return "fournisseurs/formulaire";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute Fournisseur fournisseur, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "fournisseurs/formulaire";
        }
        fournisseurService.creer(fournisseur);
        return "redirect:/fournisseurs";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        model.addAttribute("fournisseur", fournisseurService.trouverParId(id));
        return "fournisseurs/formulaire";
    }

    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id, @Valid @ModelAttribute Fournisseur fournisseur,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "fournisseurs/formulaire";
        }
        fournisseurService.modifier(id, fournisseur);
        return "redirect:/fournisseurs";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        fournisseurService.supprimer(id);
        return "redirect:/fournisseurs";
    }
}