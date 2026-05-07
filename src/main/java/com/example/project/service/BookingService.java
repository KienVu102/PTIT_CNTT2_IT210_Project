package com.example.project.service;

import com.example.project.dto.BookingRequestDTO;
import com.example.project.entity.Seat;
import com.example.project.entity.Ticket;
import com.example.project.entity.Trip;
import com.example.project.enums.SeatStatus;
import com.example.project.enums.TicketStatus;
import com.example.project.repository.SeatRepository;
import com.example.project.repository.TicketRepository;
import com.example.project.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final EmailService emailService;

    @Transactional(rollbackFor = Exception.class)
    public String processBooking(BookingRequestDTO dto) {
        // 1. Lấy ghế và KHÓA dòng dữ liệu (Pessimistic Locking - CORE-06)
        Seat seat = seatRepository.findByIdForUpdate(dto.getSeatId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế"));

        // 2. Kiểm tra ghế còn trống
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new RuntimeException("Ghế đã có người đặt trong lúc bạn thao tác!");
        }

        // 3. Cập nhật ghế thành PENDING
        seat.setStatus(SeatStatus.PENDING);
        seatRepository.save(seat);

        // 4. Lấy thông tin chuyến xe
        Trip trip = tripRepository.findById(dto.getTripId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe"));

        // 5. Tạo vé mới - CORE-06
        Ticket ticket = new Ticket();
        String ticketCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ticket.setTicketCode(ticketCode);
        ticket.setCustomerName(dto.getCustomerName());
        ticket.setCustomerPhone(dto.getCustomerPhone());
        ticket.setCustomerEmail(dto.getCustomerEmail());
        ticket.setTrip(trip);
        ticket.setSeat(seat);
        ticket.setTotalPrice(trip.getPrice());
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setBookingTime(LocalDateTime.now());

        ticketRepository.save(ticket);

        // 6. Hướng 3: Gửi email xác nhận bất đồng bộ (không block request)
        if (dto.getCustomerEmail() != null && !dto.getCustomerEmail().isBlank()) {
            emailService.sendBookingConfirmEmail(
                dto.getCustomerEmail(),
                dto.getCustomerName(),
                ticketCode,
                trip.getRoute().getFromLocation().getName(),
                trip.getRoute().getToLocation().getName(),
                trip.getDepartureTime(),
                seat.getSeatNumber(),
                trip.getPrice()
            );
        }

        return ticketCode;
    }
}
