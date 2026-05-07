package com.example.project.controller;

import com.example.project.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final TicketService ticketService;

    @GetMapping("/pending-tickets")
    public String getPendingTickets(Model model) {
        model.addAttribute("tickets", ticketService.getPendingTickets());
        return "staff/pending-tickets";
    }

    @PostMapping("/confirm-payment")
    public String confirmPayment(@RequestParam Long ticketId, Model model) {
        try {
            ticketService.confirmPayment(ticketId);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tickets", ticketService.getPendingTickets());
            return "staff/pending-tickets";
        }
        return "redirect:/staff/pending-tickets";
    }

    @PostMapping("/cancel-ticket")
    public String cancelTicket(@RequestParam Long ticketId, Model model) {
        try {
            ticketService.cancelByStaff(ticketId);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tickets", ticketService.getPendingTickets());
            return "staff/pending-tickets";
        }
        return "redirect:/staff/pending-tickets";
    }
}
