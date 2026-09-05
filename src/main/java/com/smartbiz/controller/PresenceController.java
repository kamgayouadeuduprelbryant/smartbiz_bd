package com.smartbiz.controller;

import com.smartbiz.dto.PresenceDto;
import com.smartbiz.service.EmployeService;
import com.smartbiz.service.PresenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/presences")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;
    private final EmployeService employeService;

    @GetMapping
    public String liste(@RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        LocalDate dateEffective = date != null ? date : LocalDate.now();
        Page<?> resultats = presenceService.parDate(dateEffective, PageRequest.of(page, 20));
        model.addAttribute("presencesPage", resultats);
        model.addAttribute("date", dateEffective);
        return "presences/liste";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("presenceDto", new PresenceDto());
        model.addAttribute("employes", employeService.rechercher(null, null, null, PageRequest.of(0, 500)).getContent());
        model.addAttribute("statuts", com.smartbiz.model.StatutPresence.values());
        return "presences/formulaire";
    }

    @PostMapping
    public String enregistrer(@Valid @ModelAttribute PresenceDto presenceDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("employes", employeService.rechercher(null, null, null, PageRequest.of(0, 500)).getContent());
            model.addAttribute("statuts", com.smartbiz.model.StatutPresence.values());
            return "presences/formulaire";
        }
        try {
            presenceService.enregistrer(presenceDto);
        } catch (RuntimeException ex) {
            model.addAttribute("erreur", ex.getMessage());
            model.addAttribute("employes", employeService.rechercher(null, null, null, PageRequest.of(0, 500)).getContent());
            model.addAttribute("statuts", com.smartbiz.model.StatutPresence.values());
            return "presences/formulaire";
        }
        return "redirect:/presences";
    }
}
