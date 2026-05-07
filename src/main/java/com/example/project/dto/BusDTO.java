package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusDTO {
    private Long id;
    private String plateNumber;
    private String busType;
    private Integer totalSeats;
    private String company;
    private String driverName;
}