package com.example.project.controller;

import com.example.project.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final LocationRepository locationRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("locations", locationRepository.findAll());
        return "index";
    }
}