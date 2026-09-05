package com.smartbiz.controller;

import com.smartbiz.model.Depense;
import com.smartbiz.model.Revenu;
import com.smartbiz.model.CategorieDepense;
import com.smartbiz.model.CategorieRevenu;
import com.smartbiz.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/finances")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE', 'MANAGER')")
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("stats", financeService.calculerStats());
        return "finances/index";
    }

    // ---------- Revenus ----------

    @GetMapping("/revenus")
    public String listeRevenus(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Revenu> revenus = financeService.listerRevenus(PageRequest.of(page, 10, Sort.by("date").descending()));
        model.addAttribute("revenusPage", revenus);
        return "finances/revenus";
    }

    @GetMapping("/revenus/nouveau")
    public String formulaireRevenu(Model model) {
        model.addAttribute("revenu", new Revenu());
        model.addAttribute("categories", CategorieRevenu.values());
        return "finances/revenu-formulaire";
    }

    @PostMapping("/revenus")
    public String creerRevenu(@Valid @ModelAttribute Revenu revenu, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", CategorieRevenu.values());
            return "finances/revenu-formulaire";
        }
        financeService.creerRevenu(revenu);
        return "redirect:/finances/revenus";
    }

    @PostMapping("/revenus/{id}/supprimer")
    public String supprimerRevenu(@PathVariable Long id) {
        financeService.supprimerRevenu(id);
        return "redirect:/finances/revenus";
    }

    // ---------- Depenses ----------

    @GetMapping("/depenses")
    public String listeDepenses(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Depense> depenses = financeService.listerDepenses(PageRequest.of(page, 10, Sort.by("date").descending()));
        model.addAttribute("depensesPage", depenses);
        return "finances/depenses";
    }

    @GetMapping("/depenses/nouveau")
    public String formulaireDepense(Model model) {
        model.addAttribute("depense", new Depense());
        model.addAttribute("categories", CategorieDepense.values());
        return "finances/depense-formulaire";
    }

    @PostMapping("/depenses")
    public String creerDepense(@Valid @ModelAttribute Depense depense, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", CategorieDepense.values());
            return "finances/depense-formulaire";
        }
        financeService.creerDepense(depense);
        return "redirect:/finances/depenses";
    }

    @PostMapping("/depenses/{id}/supprimer")
    public String supprimerDepense(@PathVariable Long id) {
        financeService.supprimerDepense(id);
        return "redirect:/finances/depenses";
    }
}
