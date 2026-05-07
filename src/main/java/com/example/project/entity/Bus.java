package com.example.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "buses")
@Data @NoArgsConstructor @AllArgsConstructor
public class Bus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String plateNumber;
    private String busType;
    private Integer totalSeats;
    private String company;
    private String driverName;
}