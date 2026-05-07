package com.example.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "routes")
@Data @NoArgsConstructor @AllArgsConstructor
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_location_id", nullable = false)
    private Location fromLocation;

    @ManyToOne
    @JoinColumn(name = "to_location_id", nullable = false)
    private Location toLocation;

    private Double distance;
}