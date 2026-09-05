package com.smartbiz.controller;

import com.smartbiz.service.AssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    @GetMapping
    public String page(Model model) {
        model.addAttribute("suggestions", assistantService.suggestions());
        return "assistant/index";
    }

    @PostMapping("/question")
    @ResponseBody
    public Map<String, String> poserQuestion(@RequestParam String question) {
        return Map.of("reponse", assistantService.poserQuestion(question));
    }
}
