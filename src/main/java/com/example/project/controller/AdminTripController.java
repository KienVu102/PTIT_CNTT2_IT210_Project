package com.example.project.controller;
import com.example.project.dto.TripDTO;
import com.example.project.repository.BusRepository;
import com.example.project.repository.RouteRepository;
import com.example.project.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/trips")
@RequiredArgsConstructor
public class AdminTripController {
    private final TripService tripService;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    @GetMapping
    public String listTrips(Model model) {
        model.addAttribute("trips", tripService.getAllTrips());
        model.addAttribute("routes", routeRepository.findAll());
        model.addAttribute("buses", busRepository.findAll());
        model.addAttribute("newTrip", new TripDTO());
        return "admin/trips";
    }
    @PostMapping
    public String createTrip(@ModelAttribute TripDTO tripDTO) {
        tripService.createTrip(tripDTO);
        return "redirect:/admin/trips";
    }
    @GetMapping("/delete/{id}")
    public String deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return "redirect:/admin/trips";
    }
}
