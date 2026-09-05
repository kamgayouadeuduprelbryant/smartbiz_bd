package com.smartbiz.controller;

import com.smartbiz.dto.MouvementStockDto;
import com.smartbiz.service.ProduitService;
import com.smartbiz.service.StockService;
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
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final ProduitService produitService;

    @GetMapping
    public String historique(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "15") int taille,
                              Model model) {
        Page<?> mouvements = stockService.historique(PageRequest.of(page, taille, Sort.by("dateMouvement").descending()));
        model.addAttribute("mouvementsPage", mouvements);
        return "stock/liste";
    }

    @GetMapping("/alertes")
    public String alertes(Model model) {
        model.addAttribute("stockFaible", produitService.stockFaible());
        model.addAttribute("enRupture", produitService.enRupture());
        return "stock/alertes";
    }

    @GetMapping("/nouveau")
    public String formulaireMouvement(Model model) {
        model.addAttribute("mouvementDto", new MouvementStockDto());
        model.addAttribute("produits", produitService.rechercher(null, null, null, PageRequest.of(0, 1000)).getContent());
        return "stock/formulaire";
    }

    @PostMapping
    public String enregistrer(@Valid @ModelAttribute MouvementStockDto mouvementDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("produits", produitService.rechercher(null, null, null, PageRequest.of(0, 1000)).getContent());
            return "stock/formulaire";
        }

        try {
            stockService.enregistrerMouvement(mouvementDto);
        } catch (RuntimeException ex) {
            model.addAttribute("erreur", ex.getMessage());
            model.addAttribute("produits", produitService.rechercher(null, null, null, PageRequest.of(0, 1000)).getContent());
            return "stock/formulaire";
        }

        return "redirect:/stock";
    }
}
