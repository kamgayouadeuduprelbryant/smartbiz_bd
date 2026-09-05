package com.smartbiz.controller;

import com.smartbiz.model.Departement;
import com.smartbiz.service.DepartementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/departements")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'RH', 'MANAGER')")
public class DepartementController {

    private final DepartementService departementService;

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("departements", departementService.listerTous());
        return "departements/liste";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("departement", new Departement());
        return "departements/formulaire";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute Departement departement, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "departements/formulaire";
        }
        departementService.creer(departement);
        return "redirect:/departements";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        model.addAttribute("departement", departementService.trouverParId(id));
        return "departements/formulaire";
    }

    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id, @Valid @ModelAttribute Departement departement,
                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "departements/formulaire";
        }
        departementService.modifier(id, departement);
        return "redirect:/departements";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        departementService.supprimer(id);
        return "redirect:/departements";
    }
}
