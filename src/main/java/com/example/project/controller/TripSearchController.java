package com.example.project.controller;

import com.example.project.dto.TripSearchDTO;
import com.example.project.entity.Location;
import com.example.project.repository.LocationRepository;
import com.example.project.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripSearchController {

    private final TripService tripService;
    private final LocationRepository locationRepository;

    // CORE-05: Trang tìm kiếm chuyến xe
    @GetMapping("/search")
    public String searchTrips(
            @RequestParam(required = false) Long fromId,
            @RequestParam(required = false) Long toId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        List<Location> locations = locationRepository.findAll();
        model.addAttribute("locations", locations);

        if (fromId != null && toId != null && date != null) {
            try {
                List<TripSearchDTO> trips = tripService.searchTrips(fromId, toId, date);
                model.addAttribute("trips", trips);
                model.addAttribute("fromId", fromId);
                model.addAttribute("toId", toId);
                model.addAttribute("date", date);
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }

        return "passenger/search-trips";
    }
}
