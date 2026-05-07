package com.example.project.entity;

import com.example.project.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data @NoArgsConstructor @AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String ticketCode;

    private String customerName;
    private String customerPhone;
    private String customerEmail;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    // Một ghế (trên một trip) có thể được đặt nhiều lần theo thời gian (vé hủy, vé mới...),
    // vì vậy Ticket -> Seat là ManyToOne (không unique seat_id).
    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private LocalDateTime bookingTime;
}
