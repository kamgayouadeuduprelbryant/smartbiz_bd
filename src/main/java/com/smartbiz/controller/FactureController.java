package com.smartbiz.controller;

import com.smartbiz.dto.FactureDto;
import com.smartbiz.dto.LigneFactureDto;
import com.smartbiz.dto.PaiementDto;
import com.smartbiz.model.StatutFacture;
import com.smartbiz.service.ClientService;
import com.smartbiz.service.EmailService;
import com.smartbiz.service.FactureService;
import com.smartbiz.service.ProduitService;
import com.smartbiz.util.FacturePdfGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;
    private final ClientService clientService;
    private final ProduitService produitService;
    private final EmailService emailService;

    @GetMapping
    public String liste(@RequestParam(required = false) StatutFacture statut,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int taille,
                         Model model) {
        Page<?> resultats = factureService.rechercher(statut, PageRequest.of(page, taille));
        model.addAttribute("facturesPage", resultats);
        model.addAttribute("statut", statut);
        model.addAttribute("statuts", StatutFacture.values());
        return "factures/liste";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("factureDto", new FactureDto());
        model.addAttribute("clients", clientService.rechercher(null, true, PageRequest.of(0, 500)).getContent());
        return "factures/formulaire";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute FactureDto factureDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("clients", clientService.rechercher(null, true, PageRequest.of(0, 500)).getContent());
            return "factures/formulaire";
        }
        var facture = factureService.creer(factureDto);
        return "redirect:/factures/" + facture.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("facture", factureService.trouverParId(id));
        model.addAttribute("ligneDto", new LigneFactureDto());
        model.addAttribute("paiementDto", new PaiementDto());
        model.addAttribute("produits", produitService.rechercher(null, null, null, PageRequest.of(0, 1000)).getContent());
        return "factures/detail";
    }

    @PostMapping("/{id}/lignes")
    public String ajouterLigne(@PathVariable Long id, @Valid @ModelAttribute("ligneDto") LigneFactureDto ligneDto,
                                BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            chargerDetail(id, model);
            return "factures/detail";
        }
        factureService.ajouterLigne(id, ligneDto);
        return "redirect:/factures/" + id;
    }

    @PostMapping("/{id}/lignes/{ligneId}/supprimer")
    public String supprimerLigne(@PathVariable Long id, @PathVariable Long ligneId) {
        factureService.supprimerLigne(id, ligneId);
        return "redirect:/factures/" + id;
    }

    @PostMapping("/{id}/emettre")
    public String emettre(@PathVariable Long id, Model model) {
        try {
            factureService.emettre(id);
        } catch (RuntimeException ex) {
            model.addAttribute("erreur", ex.getMessage());
            chargerDetail(id, model);
            return "factures/detail";
        }
        return "redirect:/factures/" + id;
    }

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id) {
        factureService.annuler(id);
        return "redirect:/factures/" + id;
    }

    @PostMapping("/{id}/paiements")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE', 'MANAGER')")
    public String enregistrerPaiement(@PathVariable Long id, @Valid @ModelAttribute("paiementDto") PaiementDto paiementDto,
                                       BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            chargerDetail(id, model);
            return "factures/detail";
        }
        try {
            factureService.enregistrerPaiement(id, paiementDto);
        } catch (RuntimeException ex) {
            model.addAttribute("erreur", ex.getMessage());
            chargerDetail(id, model);
            return "factures/detail";
        }
        return "redirect:/factures/" + id;
    }

    @PostMapping("/{id}/envoyer-email")
    public String envoyerParEmail(@PathVariable Long id) {
        var facture = factureService.trouverParId(id);
        emailService.envoyerFacture(facture);
        return "redirect:/factures/" + id;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exporterPdf(@PathVariable Long id) {
        var facture = factureService.trouverParId(id);
        byte[] pdf = FacturePdfGenerator.generer(facture);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + facture.getNumero() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private void chargerDetail(Long id, Model model) {
        model.addAttribute("facture", factureService.trouverParId(id));
        model.addAttribute("produits", produitService.rechercher(null, null, null, PageRequest.of(0, 1000)).getContent());
        if (!model.containsAttribute("ligneDto")) model.addAttribute("ligneDto", new LigneFactureDto());
        if (!model.containsAttribute("paiementDto")) model.addAttribute("paiementDto", new PaiementDto());
    }
}
