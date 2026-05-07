package com.example.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Hướng 3: gửi email xác nhận đặt vé (bất đồng bộ)
    @Async
    public void sendBookingConfirmEmail(
            String to,
            String customerName,
            String ticketCode,
            String from,
            String toLocation,
            LocalDateTime departureTime,
            String seatNumber,
            Double price
    ) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[Bus Ticket Pro] Xác nhận đặt vé - Mã vé: " + ticketCode);
            message.setText(String.format(
                    "Xin chào %s,\n\n" +
                    "Bạn đã đặt vé thành công.\n\n" +
                    "Mã vé: %s\n" +
                    "Tuyến: %s -> %s\n" +
                    "Khởi hành: %s\n" +
                    "Ghế số: %s\n" +
                    "Giá vé: %.0f VND\n\n" +
                    "Vé đang ở trạng thái CHỜ THANH TOÁN. Vui lòng thanh toán tại quầy trong 30 phút.\n\n" +
                    "Trân trọng,\nBus Ticket Pro",
                    customerName,
                    ticketCode,
                    from,
                    toLocation,
                    departureTime.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")),
                    seatNumber,
                    price
            ));
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EMAIL] Lỗi gửi email đặt vé: " + e.getMessage());
        }
    }

    // Hướng 3: gửi email xác nhận thanh toán (bất đồng bộ)
    @Async
    public void sendPaymentConfirmEmail(
            String to,
            String customerName,
            String ticketCode,
            String from,
            String toLocation,
            LocalDateTime departureTime,
            String seatNumber,
            Double price
    ) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[Bus Ticket Pro] Thanh toán thành công - Mã vé: " + ticketCode);
            message.setText(String.format(
                    "Xin chào %s,\n\n" +
                    "Thanh toán vé của bạn đã được xác nhận.\n\n" +
                    "Mã vé: %s\n" +
                    "Tuyến: %s -> %s\n" +
                    "Khởi hành: %s\n" +
                    "Ghế số: %s\n" +
                    "Đã thanh toán: %.0f VND\n\n" +
                    "Chúc bạn có chuyến đi vui vẻ.\n\n" +
                    "Trân trọng,\nBus Ticket Pro",
                    customerName,
                    ticketCode,
                    from,
                    toLocation,
                    departureTime.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")),
                    seatNumber,
                    price
            ));
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EMAIL] Lỗi gửi email thanh toán: " + e.getMessage());
        }
    }
}

