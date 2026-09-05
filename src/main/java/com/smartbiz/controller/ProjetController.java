package com.smartbiz.controller;

import com.smartbiz.dto.ProjetDto;
import com.smartbiz.dto.TacheDto;
import com.smartbiz.model.StatutTache;
import com.smartbiz.service.EmployeService;
import com.smartbiz.service.ProjetService;
import com.smartbiz.service.TacheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projets")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;
    private final TacheService tacheService;
    private final EmployeService employeService;

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("projets", projetService.listerTous());
        return "projets/liste";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("projetDto", new ProjetDto());
        model.addAttribute("employes", employeService.rechercher(null, null, null, PageRequest.of(0, 500)).getContent());
        return "projets/formulaire";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute ProjetDto projetDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("employes", employeService.rechercher(null, null, null, PageRequest.of(0, 500)).getContent());
            return "projets/formulaire";
        }
        var projet = projetService.creer(projetDto);
        return "redirect:/projets/" + projet.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var projet = projetService.trouverParId(id);

        Map<StatutTache, List<com.smartbiz.model.Tache>> colonnes = Arrays.stream(StatutTache.values())
                .collect(Collectors.toMap(
                        s -> s,
                        s -> projet.getTaches().stream().filter(t -> t.getStatut() == s).collect(Collectors.toList())
                ));

        model.addAttribute("projet", projet);
        model.addAttribute("colonnes", colonnes);
        model.addAttribute("statutsTache", StatutTache.values());
        model.addAttribute("tacheDto", new TacheDto());
        model.addAttribute("employes", employeService.rechercher(null, null, null, PageRequest.of(0, 500)).getContent());
        return "projets/detail";
    }

    @PostMapping("/{id}/taches")
    public String ajouterTache(@PathVariable Long id, @Valid @ModelAttribute("tacheDto") TacheDto tacheDto,
                                BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return detail(id, model);
        }
        tacheService.creer(id, tacheDto);
        return "redirect:/projets/" + id;
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        projetService.supprimer(id);
        return "redirect:/projets";
    }
}
