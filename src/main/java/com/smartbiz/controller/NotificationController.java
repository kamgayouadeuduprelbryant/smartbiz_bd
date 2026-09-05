package com.smartbiz.controller;

import com.smartbiz.dto.NotificationDto;
import com.smartbiz.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public String page(org.springframework.ui.Model model) {
        model.addAttribute("notifications",
                notificationService.pourUtilisateurConnecte(PageRequest.of(0, 50, Sort.by("dateCreation").descending())));
        return "notifications/liste";
    }

    /** Interroge periodiquement pour rafraichir la pastille du header (voir static/js/notifications.js). */
    @GetMapping("/recentes")
    @ResponseBody
    public Map<String, Object> recentes() {
        List<NotificationDto> notifications =
                notificationService.pourUtilisateurConnecte(PageRequest.of(0, 8, Sort.by("dateCreation").descending()));
        return Map.of(
                "nonLues", notificationService.compterNonLues(),
                "notifications", notifications
        );
    }

    @PostMapping("/{id}/lue")
    @ResponseBody
    public void marquerCommeLue(@PathVariable Long id) {
        notificationService.marquerCommeLue(id);
    }
}
