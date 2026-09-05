package com.smartbiz.controller;

import com.smartbiz.model.Client;
import com.smartbiz.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public String liste(@RequestParam(required = false) String recherche,
                         @RequestParam(required = false) Boolean actif,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int taille,
                         Model model) {

        Page<Client> resultats = clientService.rechercher(recherche, actif,
                PageRequest.of(page, taille, Sort.by("nom").ascending()));

        model.addAttribute("clientsPage", resultats);
        model.addAttribute("recherche", recherche);
        model.addAttribute("actif", actif);
        return "clients/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.trouverParId(id));
        return "clients/detail";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("client", new Client());
        return "clients/formulaire";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute Client client, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "clients/formulaire";
        }
        clientService.creer(client);
        return "redirect:/clients";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.trouverParId(id));
        return "clients/formulaire";
    }

    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id, @Valid @ModelAttribute Client client, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "clients/formulaire";
        }
        clientService.modifier(id, client);
        return "redirect:/clients";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        clientService.supprimer(id);
        return "redirect:/clients";
    }
}
