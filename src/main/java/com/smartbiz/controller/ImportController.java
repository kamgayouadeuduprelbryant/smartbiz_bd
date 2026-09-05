package com.smartbiz.controller;

import com.smartbiz.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    // ---------- Employes ----------

    @GetMapping("/employes/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public String formulaireEmployes() {
        return "employes/import";
    }

    @PostMapping("/employes/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public String importerEmployes(@RequestParam("fichier") MultipartFile fichier, Model model) {
        if (fichier.isEmpty()) {
            model.addAttribute("erreur", "Veuillez selectionner un fichier Excel.");
            return "employes/import";
        }
        try {
            model.addAttribute("resultat", importService.importerEmployes(fichier));
        } catch (RuntimeException ex) {
            model.addAttribute("erreur", "Le fichier n'a pas pu etre lu : " + ex.getMessage());
        }
        return "employes/import";
    }

    // ---------- Produits ----------

    @GetMapping("/produits/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String formulaireProduits() {
        return "produits/import";
    }

    @PostMapping("/produits/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String importerProduits(@RequestParam("fichier") MultipartFile fichier, Model model) {
        if (fichier.isEmpty()) {
            model.addAttribute("erreur", "Veuillez selectionner un fichier Excel.");
            return "produits/import";
        }
        try {
            model.addAttribute("resultat", importService.importerProduits(fichier));
        } catch (RuntimeException ex) {
            model.addAttribute("erreur", "Le fichier n'a pas pu etre lu : " + ex.getMessage());
        }
        return "produits/import";
    }

    // ---------- Clients ----------

    @GetMapping("/clients/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMMERCIAL')")
    public String formulaireClients() {
        return "clients/import";
    }

    @PostMapping("/clients/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMMERCIAL')")
    public String importerClients(@RequestParam("fichier") MultipartFile fichier, Model model) {
        if (fichier.isEmpty()) {
            model.addAttribute("erreur", "Veuillez selectionner un fichier Excel.");
            return "clients/import";
        }
        try {
            model.addAttribute("resultat", importService.importerClients(fichier));
        } catch (RuntimeException ex) {
            model.addAttribute("erreur", "Le fichier n'a pas pu etre lu : " + ex.getMessage());
        }
        return "clients/import";
    }
}
