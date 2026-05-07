package com.example.project.entity;

import com.example.project.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Data @NoArgsConstructor @AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;
}