package com.example.project.controller;
import com.example.project.enums.TicketStatus;
import com.example.project.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {
    private final TicketRepository ticketRepository;
    @GetMapping
    public String showReport(Model model) {
        Double revenue = ticketRepository.getTotalRevenue();
        model.addAttribute("revenue", revenue != null ? revenue : 0.0);
        model.addAttribute("paidCount", ticketRepository.countByStatus(TicketStatus.PAID));
        model.addAttribute("pendingCount", ticketRepository.countByStatus(TicketStatus.PENDING));
        model.addAttribute("cancelledCount", ticketRepository.countByStatus(TicketStatus.CANCELLED));
        model.addAttribute("topRoutes", ticketRepository.getTopRoutes());
        return "admin/report";
    }
}
