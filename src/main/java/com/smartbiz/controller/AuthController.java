package com.smartbiz.controller;

import com.smartbiz.dto.RegisterRequest;
import com.smartbiz.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurService utilisateurService;

    @GetMapping("/auth/login")
    public String login() {
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !"anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String formulaireInscription(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String inscrire(@Valid @ModelAttribute RegisterRequest registerRequest,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            utilisateurService.inscrire(registerRequest);
        } catch (RuntimeException ex) {
            model.addAttribute("erreur", ex.getMessage());
            return "auth/register";
        }

        return "redirect:/auth/login?inscription";
    }

    @GetMapping("/auth/mot-de-passe-oublie")
    public String motDePasseOublie() {
        return "auth/mot-de-passe-oublie";
    }

    @PostMapping("/auth/mot-de-passe-oublie")
    public String demanderReinitialisation(@RequestParam String email) {
        utilisateurService.demanderReinitialisationMotDePasse(email);
        // Message identique que le compte existe ou non : evite de reveler
        // quels emails sont enregistres (protection contre l'enumeration de comptes).
        return "redirect:/auth/login?recuperation";
    }
}