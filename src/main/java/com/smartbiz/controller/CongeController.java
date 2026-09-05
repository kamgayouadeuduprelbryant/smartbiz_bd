package com.smartbiz.controller;

import com.smartbiz.dto.CongeDto;
import com.smartbiz.model.StatutConge;
import com.smartbiz.service.CongeService;
import com.smartbiz.service.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/conges")
@RequiredArgsConstructor
public class CongeController {

    private final CongeService congeService;
    private final EmployeService employeService;

    @GetMapping
    public String liste(@RequestParam(required = false) StatutConge statut,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        Page<?> resultats = congeService.rechercher(statut, PageRequest.of(page, 10));
        model.addAttribute("congesPage", resultats);
        model.addAttribute("statut", statut);
        model.addAttribute("statuts", StatutConge.values());
        return "conges/liste";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("congeDto", new CongeDto());
        model.addAttribute("employes", employeService.rechercher(null, null, null, PageRequest.of(0, 500)).getContent());
        model.addAttribute("types", com.smartbiz.model.TypeConge.values());
        return "conges/formulaire";
    }

    @PostMapping
    public String demander(@Valid @ModelAttribute CongeDto congeDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("employes", employeService.rechercher(null, null, null, PageRequest.of(0, 500)).getContent());
            model.addAttribute("types", com.smartbiz.model.TypeConge.values());
            return "conges/formulaire";
        }
        try {
            congeService.demander(congeDto);
        } catch (RuntimeException ex) {
            model.addAttribute("erreur", ex.getMessage());
            model.addAttribute("employes", employeService.rechercher(null, null, null, PageRequest.of(0, 500)).getContent());
            model.addAttribute("types", com.smartbiz.model.TypeConge.values());
            return "conges/formulaire";
        }
        return "redirect:/conges";
    }

    @PostMapping("/{id}/approuver")
    public String approuver(@PathVariable Long id, @RequestParam(required = false) String commentaire) {
        congeService.approuver(id, commentaire);
        return "redirect:/conges";
    }

    @PostMapping("/{id}/refuser")
    public String refuser(@PathVariable Long id, @RequestParam(required = false) String commentaire) {
        congeService.refuser(id, commentaire);
        return "redirect:/conges";
    }
}
