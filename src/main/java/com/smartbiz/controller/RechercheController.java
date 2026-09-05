package com.smartbiz.controller;

import com.smartbiz.dto.RechercheResultatDto;
import com.smartbiz.service.RechercheService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RechercheController {

    private final RechercheService rechercheService;

    @GetMapping("/recherche")
    @ResponseBody
    public List<RechercheResultatDto> rechercher(@RequestParam(name = "q", required = false) String motCle) {
        if (!StringUtils.hasText(motCle) || motCle.trim().length() < 2) {
            return List.of();
        }
        return rechercheService.rechercher(motCle.trim());
    }
}
