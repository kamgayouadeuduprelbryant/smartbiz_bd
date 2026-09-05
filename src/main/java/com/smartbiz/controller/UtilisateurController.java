package com.smartbiz.controller;

import com.smartbiz.dto.UtilisateurAdminDto;
import com.smartbiz.service.UtilisateurAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurAdminService utilisateurAdminService;

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("utilisateurs", utilisateurAdminService.listerTous());
        return "utilisateurs/liste";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        var utilisateur = utilisateurAdminService.trouverParId(id);

        UtilisateurAdminDto dto = new UtilisateurAdminDto();
        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setActif(utilisateur.isActif());
        dto.setRoleIds(utilisateur.getRoles().stream().map(r -> r.getId()).toList());

        model.addAttribute("utilisateurDto", dto);
        model.addAttribute("roles", utilisateurAdminService.listerRoles());
        return "utilisateurs/formulaire";
    }

    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id, @ModelAttribute UtilisateurAdminDto dto) {
        utilisateurAdminService.modifier(id, dto);
        return "redirect:/utilisateurs";
    }

    @PostMapping("/{id}/basculer-activation")
    public String basculerActivation(@PathVariable Long id) {
        utilisateurAdminService.basculerActivation(id);
        return "redirect:/utilisateurs";
    }
}
