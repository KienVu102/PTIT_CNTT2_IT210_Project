package com.example.project.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TripDTO {
    private Long id;
    private Long routeId;
    private Long busId;
    private LocalDateTime departureTime;
    private Double price;
}
