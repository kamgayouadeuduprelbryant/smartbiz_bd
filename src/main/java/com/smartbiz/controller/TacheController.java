package com.smartbiz.controller;

import com.smartbiz.model.StatutTache;
import com.smartbiz.service.TacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/taches")
@RequiredArgsConstructor
public class TacheController {

    private final TacheService tacheService;

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("taches", tacheService.listerToutes());
        return "taches/liste";
    }

    /**
     * Change le statut d'une tache (colonne Kanban) en AJAX, sans recharger la page.
     * Voir static/js/kanban.js pour le glisser-deposer cote client.
     */
    @PostMapping("/{id}/statut")
    @ResponseBody
    public void changerStatut(@PathVariable Long id, @RequestParam StatutTache statut) {
        tacheService.changerStatut(id, statut);
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        var tache = tacheService.trouverParId(id);
        Long projetId = tache.getProjet().getId();
        tacheService.supprimer(id);
        return "redirect:/projets/" + projetId;
    }
}
