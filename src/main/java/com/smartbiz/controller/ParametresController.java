package com.smartbiz.controller;

import com.smartbiz.dto.ChangerMotDePasseDto;
import com.smartbiz.dto.ProfilDto;
import com.smartbiz.service.AuditService;
import com.smartbiz.service.ProfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/parametres")
@RequiredArgsConstructor
public class ParametresController {

    private final ProfilService profilService;
    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String hub() {
        return "redirect:/parametres/audit";
    }

    @GetMapping("/profil")
    public String profil(Authentication authentication, Model model) {
        var utilisateur = profilService.trouverParEmail(authentication.getName());

        ProfilDto dto = new ProfilDto();
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setTelephone(utilisateur.getTelephone());

        model.addAttribute("profilDto", dto);
        model.addAttribute("compteGoogle", utilisateur.isCompteGoogle());
        return "parametres/profil";
    }

    @PostMapping("/profil")
    public String modifierProfil(Authentication authentication, @Valid @ModelAttribute ProfilDto profilDto,
                                  BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "parametres/profil";
        }
        try {
            profilService.modifierProfil(authentication.getName(), profilDto);
        } catch (RuntimeException ex) {
            model.addAttribute("erreur", ex.getMessage());
            return "parametres/profil";
        }
        return "redirect:/parametres/profil?succes";
    }

    @GetMapping("/mot-de-passe")
    public String motDePasse(Model model) {
        model.addAttribute("changerMotDePasseDto", new ChangerMotDePasseDto());
        return "parametres/mot-de-passe";
    }

    @PostMapping("/mot-de-passe")
    public String changerMotDePasse(Authentication authentication, @Valid @ModelAttribute ChangerMotDePasseDto dto,
                                     BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "parametres/mot-de-passe";
        }
        try {
            profilService.changerMotDePasse(authentication.getName(), dto);
        } catch (RuntimeException ex) {
            model.addAttribute("erreur", ex.getMessage());
            return "parametres/mot-de-passe";
        }
        return "redirect:/parametres/mot-de-passe?succes";
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public String audit(@RequestParam(defaultValue = "0") int page, Model model) {
        var historique = auditService.historique(PageRequest.of(page, 25, Sort.by("dateAction").descending()));
        model.addAttribute("auditPage", historique);
        return "parametres/audit";
    }
}
