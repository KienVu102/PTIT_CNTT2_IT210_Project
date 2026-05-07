package com.example.project.controller;
import com.example.project.dto.TripDTO;
import com.example.project.repository.BusRepository;
import com.example.project.repository.LocationRepository;
import com.example.project.repository.RouteRepository;
import com.example.project.repository.TripRepository;
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
    private final LocationRepository locationRepository;

    private final TripRepository tripRepository;

    @GetMapping
    public String listTrips(Model model) {
        populateTripOptions(model);
        model.addAttribute("trips", tripService.getAllTrips());
        model.addAttribute("newTrip", new TripDTO());
        return "admin/trips";
    }

    @PostMapping
    public String createTrip(@ModelAttribute("newTrip") TripDTO tripDTO, Model model) {
        try {
            tripService.createTrip(tripDTO);
            return "redirect:/admin/trips";
        } catch (Exception e) {
            populateTripOptions(model);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("trips", tripService.getAllTrips());
            return "admin/trips";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return "redirect:/admin/trips";
    }

    @GetMapping("/edit/{id}")
    public String editTripForm(@PathVariable Long id, Model model) {
        var trip = tripRepository.findById(id).orElse(null);
        if (trip == null) {
            return "redirect:/admin/trips";
        }

        TripDTO dto = new TripDTO();
        dto.setId(trip.getId());
        dto.setRouteId(trip.getRoute().getId());
        dto.setFromLocationId(trip.getRoute().getFromLocation().getId());
        dto.setToLocationId(trip.getRoute().getToLocation().getId());
        dto.setBusId(trip.getBus().getId());
        dto.setDepartureTime(trip.getDepartureTime());
        dto.setPrice(trip.getPrice());

        populateTripOptions(model);
        model.addAttribute("trip", trip);
        model.addAttribute("tripDTO", dto);
        model.addAttribute("hasTickets", tripService.hasTickets(id));
        return "admin/trip-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateTrip(@PathVariable Long id, @ModelAttribute TripDTO tripDTO, Model model) {
        try {
            tripService.updateTrip(id, tripDTO);
            return "redirect:/admin/trips";
        } catch (Exception e) {
            var trip = tripRepository.findById(id).orElse(null);
            model.addAttribute("error", e.getMessage());
            populateTripOptions(model);
            model.addAttribute("trip", trip);
            model.addAttribute("tripDTO", tripDTO);
            model.addAttribute("hasTickets", tripService.hasTickets(id));
            return "admin/trip-edit";
        }
    }

    private void populateTripOptions(Model model) {
        model.addAttribute("routes", routeRepository.findAll());
        model.addAttribute("locations", locationRepository.findAll());
        model.addAttribute("buses", busRepository.findAll());
    }
}
