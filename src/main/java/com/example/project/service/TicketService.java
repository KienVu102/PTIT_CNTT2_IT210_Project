package com.example.project.service;

import com.example.project.dto.TicketDetailDTO;
import com.example.project.entity.Seat;
import com.example.project.entity.Ticket;
import com.example.project.enums.SeatStatus;
import com.example.project.enums.TicketStatus;
import com.example.project.repository.SeatRepository;
import com.example.project.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final EmailService emailService;

    // CORE-07: Tra cứu vé bằng mã vé + SĐT và trả về thông tin JOIN đầy đủ
    public TicketDetailDTO getTicketDetail(String code, String phone) {
        Ticket ticket = ticketRepository.findByTicketCodeAndCustomerPhone(code, phone)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin vé phù hợp"));

        return new TicketDetailDTO(
                ticket.getId(),
                ticket.getTicketCode(),
                ticket.getCustomerName(),
                ticket.getCustomerPhone(),
                ticket.getTrip().getBus().getPlateNumber(),
                ticket.getTrip().getBus().getBusType(),
                ticket.getTrip().getBus().getDriverName(),
                ticket.getTrip().getRoute().getFromLocation().getName(),
                ticket.getTrip().getRoute().getToLocation().getName(),
                ticket.getTrip().getDepartureTime(),
                ticket.getSeat().getSeatNumber(),
                ticket.getTotalPrice(),
                ticket.getStatus()
        );
    }

    // CORE-08: Staff xem danh sách vé chờ thanh toán
    public List<Ticket> getPendingTickets() {
        return ticketRepository.findByStatus(TicketStatus.PENDING);
    }

    // CORE-08: Staff hủy vé (thủ công) và giải phóng ghế
    @Transactional
    public void cancelByStaff(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé"));

        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy vé đang chờ thanh toán");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);

        Seat seat = ticket.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);
    }

    // CORE-09: Hành khách hủy vé trước 12 tiếng và giải phóng ghế
    @Transactional
    public void cancelProcess(Long ticketId, String phone) {
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé"));

        if (!ticket.getCustomerPhone().equals(phone)) {
            throw new RuntimeException("Số điện thoại không khớp với thông tin vé");
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new RuntimeException("Vé đã bị hủy");
        }

        LocalDateTime deadline = ticket.getTrip().getDepartureTime().minusHours(12);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new RuntimeException("Chỉ có thể hủy vé trước 12 tiếng so với giờ khởi hành");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);

        Seat seat = ticket.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);
    }

    // CORE-08: Staff xác nhận thanh toán PENDING -> PAID; ghế PENDING -> BOOKED
    @Transactional
    public void confirmPayment(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé"));

        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new RuntimeException("Vé không ở trạng thái chờ thanh toán");
        }

        ticket.setStatus(TicketStatus.PAID);
        ticketRepository.save(ticket);

        Seat seat = ticket.getSeat();
        seat.setStatus(SeatStatus.BOOKED);
        seatRepository.save(seat);

        if (ticket.getCustomerEmail() != null && !ticket.getCustomerEmail().isBlank()) {
            emailService.sendPaymentConfirmEmail(
                    ticket.getCustomerEmail(),
                    ticket.getCustomerName(),
                    ticket.getTicketCode(),
                    ticket.getTrip().getRoute().getFromLocation().getName(),
                    ticket.getTrip().getRoute().getToLocation().getName(),
                    ticket.getTrip().getDepartureTime(),
                    ticket.getSeat().getSeatNumber(),
                    ticket.getTotalPrice()
            );
        }
    }

    // Hướng 3: Cron job tự hủy vé PENDING quá 30 phút và giải phóng ghế (mỗi 10 phút)
    @Scheduled(fixedDelay = 600000)
    @Transactional
    public void autoCancelExpiredTickets() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        List<Ticket> expiredTickets = ticketRepository.findPendingOlderThan(threshold);

        for (Ticket ticket : expiredTickets) {
            // an toàn nếu có tiến trình khác vừa xác nhận/hủy
            if (ticket.getStatus() != TicketStatus.PENDING) {
                continue;
            }
            ticket.setStatus(TicketStatus.CANCELLED);
            ticketRepository.save(ticket);

            Seat seat = ticket.getSeat();
            seat.setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(seat);
        }
    }
}

