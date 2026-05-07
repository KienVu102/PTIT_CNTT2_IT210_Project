package com.example.project.controller;

import com.example.project.dto.ProfileDTO;
import com.example.project.entity.User;
import com.example.project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String profile(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.getProfile(username);
        model.addAttribute("user", user);
        model.addAttribute("profileDTO", toProfileDTO(user));
        return "profile";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute ProfileDTO dto,
                                BindingResult result,
                                Authentication authentication,
                                Model model) {
        String username = authentication.getName();
        if (result.hasErrors()) {
            model.addAttribute("user", userService.getProfile(username));
            return "profile";
        }
        try {
            userService.updateProfile(username, dto);
            model.addAttribute("message", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("user", userService.getProfile(username));
        model.addAttribute("profileDTO", dto);
        return "profile";
    }

    private ProfileDTO toProfileDTO(User user) {
        ProfileDTO dto = new ProfileDTO();
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
