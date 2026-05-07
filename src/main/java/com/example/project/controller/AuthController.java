package com.example.project.controller;

import com.example.project.dto.RegisterDTO;
import com.example.project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // CORE-01: Trang đăng nhập
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // CORE-01: Trang đăng ký
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    // CORE-01: Xử lý đăng ký
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterDTO dto,
                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(dto);
            return "redirect:/auth/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}
