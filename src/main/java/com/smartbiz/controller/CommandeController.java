package com.smartbiz.controller;

import com.smartbiz.dto.CommandeDto;
import com.smartbiz.dto.LigneCommandeDto;
import com.smartbiz.model.StatutCommande;
import com.smartbiz.service.ClientService;
import com.smartbiz.service.CommandeService;
import com.smartbiz.service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/commandes")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;
    private final ClientService clientService;
    private final ProduitService produitService;

    @GetMapping
    public String liste(@RequestParam(required = false) StatutCommande statut,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int taille,
                         Model model) {
        Page<?> resultats = commandeService.rechercher(statut, PageRequest.of(page, taille));
        model.addAttribute("commandesPage", resultats);
        model.addAttribute("statut", statut);
        model.addAttribute("statuts", StatutCommande.values());
        return "commandes/liste";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("commandeDto", new CommandeDto());
        model.addAttribute("clients", clientService.rechercher(null, true, PageRequest.of(0, 500)).getContent());
        return "commandes/formulaire";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute CommandeDto commandeDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("clients", clientService.rechercher(null, true, PageRequest.of(0, 500)).getContent());
            return "commandes/formulaire";
        }
        var commande = commandeService.creer(commandeDto);
        return "redirect:/commandes/" + commande.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("commande", commandeService.trouverParId(id));
        model.addAttribute("ligneDto", new LigneCommandeDto());
        model.addAttribute("produits", produitService.rechercher(null, null, null, PageRequest.of(0, 1000)).getContent());
        model.addAttribute("statuts", StatutCommande.values());
        return "commandes/detail";
    }

    @PostMapping("/{id}/lignes")
    public String ajouterLigne(@PathVariable Long id, @Valid @ModelAttribute("ligneDto") LigneCommandeDto ligneDto,
                                BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("commande", commandeService.trouverParId(id));
            model.addAttribute("produits", produitService.rechercher(null, null, null, PageRequest.of(0, 1000)).getContent());
            model.addAttribute("statuts", StatutCommande.values());
            return "commandes/detail";
        }
        commandeService.ajouterLigne(id, ligneDto);
        return "redirect:/commandes/" + id;
    }

    @PostMapping("/{id}/lignes/{ligneId}/supprimer")
    public String supprimerLigne(@PathVariable Long id, @PathVariable Long ligneId) {
        commandeService.supprimerLigne(id, ligneId);
        return "redirect:/commandes/" + id;
    }

    @PostMapping("/{id}/statut")
    public String changerStatut(@PathVariable Long id, @RequestParam StatutCommande statut, Model model) {
        try {
            commandeService.changerStatut(id, statut);
        } catch (RuntimeException ex) {
            model.addAttribute("erreur", ex.getMessage());
            model.addAttribute("commande", commandeService.trouverParId(id));
            model.addAttribute("ligneDto", new LigneCommandeDto());
            model.addAttribute("produits", produitService.rechercher(null, null, null, PageRequest.of(0, 1000)).getContent());
            model.addAttribute("statuts", StatutCommande.values());
            return "commandes/detail";
        }
        return "redirect:/commandes/" + id;
    }
}
