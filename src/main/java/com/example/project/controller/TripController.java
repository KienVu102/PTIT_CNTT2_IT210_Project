package com.example.project.controller;

import com.example.project.dto.SeatMapDTO;
import com.example.project.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class TripController {

    private final SeatService seatService;

    // Client gọi API này kèm tham số tripId (VD: /api/seats?tripId=1)
    @GetMapping
    public List<SeatMapDTO> getAvailableSeats(@RequestParam Long tripId) {
        return seatService.getAvailableSeats(tripId);
    }
}