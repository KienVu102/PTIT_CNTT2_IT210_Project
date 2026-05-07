package com.example.project.service;

import com.example.project.dto.SeatMapDTO;
import com.example.project.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    public List<SeatMapDTO> getAvailableSeats(Long tripId) {
        return seatRepository.findByTripId(tripId).stream()
                .map(seat -> new SeatMapDTO(seat.getId(), seat.getSeatNumber(), seat.getStatus()))
                .collect(Collectors.toList());
    }
}