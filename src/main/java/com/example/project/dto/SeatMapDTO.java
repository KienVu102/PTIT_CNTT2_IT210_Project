package com.example.project.dto;

import com.example.project.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapDTO {
    private Long id;
    private String seatNumber;
    private SeatStatus status;
}