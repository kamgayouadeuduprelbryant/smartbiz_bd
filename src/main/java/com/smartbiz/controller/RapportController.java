package com.smartbiz.controller;

import com.smartbiz.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rapports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMPTABLE')")
public class RapportController {

    private final RapportService rapportService;

    @GetMapping
    public String page() {
        return "rapports/index";
    }

    @GetMapping("/employes/excel")
    public ResponseEntity<byte[]> exporterEmployes() {
        return telecharger(rapportService.exporterEmployes(), "employes.xlsx");
    }

    @GetMapping("/stock/excel")
    public ResponseEntity<byte[]> exporterStock() {
        return telecharger(rapportService.exporterStock(), "stock.xlsx");
    }

    @GetMapping("/fournisseurs/excel")
    public ResponseEntity<byte[]> exporterFournisseurs() {
        return telecharger(rapportService.exporterFournisseurs(), "fournisseurs.xlsx");
    }

    @GetMapping("/commandes/excel")
    public ResponseEntity<byte[]> exporterCommandes() {
        return telecharger(rapportService.exporterCommandes(), "commandes.xlsx");
    }

    @GetMapping("/factures/excel")
    public ResponseEntity<byte[]> exporterFactures() {
        return telecharger(rapportService.exporterFactures(), "factures.xlsx");
    }

    @GetMapping("/finances/excel")
    public ResponseEntity<byte[]> exporterFinances() {
        return telecharger(rapportService.exporterFinances(), "finances.xlsx");
    }

    private ResponseEntity<byte[]> telecharger(byte[] contenu, String nomFichier) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichier + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(contenu);
    }
}
