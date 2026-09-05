package com.smartbiz.controller;

import com.smartbiz.dto.DashboardStatsDto;
import com.smartbiz.service.DashboardService;
import com.smartbiz.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final FinanceService financeService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.calculerStats());
        model.addAttribute("financeStats", financeService.calculerStats());
        return "dashboard/index";
    }

    /**
     * Endpoint AJAX permettant de rafraichir les cartes du dashboard sans
     * recharger toute la page (point 23 du cahier des charges).
     * Voir static/js/dashboard.js pour l'appel Fetch correspondant.
     */
    @GetMapping("/dashboard/stats")
    @ResponseBody
    public DashboardStatsDto stats() {
        return dashboardService.calculerStats();
    }
}
