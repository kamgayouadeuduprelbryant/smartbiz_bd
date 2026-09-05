package com.smartbiz.controller;

import com.smartbiz.dto.EmployeDto;
import com.smartbiz.model.StatutEmploye;
import com.smartbiz.service.DepartementService;
import com.smartbiz.service.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'RH', 'MANAGER')")
public class EmployeController {

    private final EmployeService employeService;
    private final DepartementService departementService;

    @GetMapping
    public String liste(@RequestParam(required = false) String recherche,
                         @RequestParam(required = false) Long departementId,
                         @RequestParam(required = false) StatutEmploye statut,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int taille,
                         Model model) {

        Page<?> resultats = employeService.rechercher(
                recherche, departementId, statut,
                PageRequest.of(page, taille, Sort.by("nom").ascending()));

        model.addAttribute("employesPage", resultats);
        model.addAttribute("departements", departementService.listerTous());
        model.addAttribute("recherche", recherche);
        model.addAttribute("departementId", departementId);
        model.addAttribute("statut", statut);
        return "employes/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("employe", employeService.trouverParId(id));
        return "employes/detail";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("employeDto", new EmployeDto());
        model.addAttribute("departements", departementService.listerTous());
        return "employes/formulaire";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute EmployeDto employeDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departements", departementService.listerTous());
            return "employes/formulaire";
        }
        employeService.creer(employeDto);
        return "redirect:/employes";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        var employe = employeService.trouverParId(id);

        EmployeDto dto = new EmployeDto();
        dto.setId(employe.getId());
        dto.setMatricule(employe.getMatricule());
        dto.setNom(employe.getNom());
        dto.setPrenom(employe.getPrenom());
        dto.setEmail(employe.getEmail());
        dto.setTelephone(employe.getTelephone());
        dto.setPoste(employe.getPoste());
        dto.setDepartementId(employe.getDepartement() != null ? employe.getDepartement().getId() : null);
        dto.setSalaire(employe.getSalaire());
        dto.setDateEmbauche(employe.getDateEmbauche());
        dto.setStatut(employe.getStatut());

        model.addAttribute("employeDto", dto);
        model.addAttribute("departements", departementService.listerTous());
        return "employes/formulaire";
    }

    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id, @Valid @ModelAttribute EmployeDto employeDto,
                            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departements", departementService.listerTous());
            return "employes/formulaire";
        }
        employeService.modifier(id, employeDto);
        return "redirect:/employes";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        employeService.supprimer(id);
        return "redirect:/employes";
    }
}
