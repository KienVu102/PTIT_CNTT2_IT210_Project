package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripSearchDTO {
    private Long id;
    private String fromLocation;
    private String toLocation;
    private LocalDateTime departureTime;
    private Double price;
    private String plateNumber;
    private String busType;
    private int availableSeats;
}
