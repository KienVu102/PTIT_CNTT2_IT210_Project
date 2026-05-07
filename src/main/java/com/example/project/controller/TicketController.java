package com.example.project.controller;

import com.example.project.dto.BookingRequestDTO;
import com.example.project.dto.TicketDetailDTO;
import com.example.project.entity.Trip;
import com.example.project.repository.TripRepository;
import com.example.project.service.BookingService;
import com.example.project.service.SeatService;
import com.example.project.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final BookingService bookingService;
    private final TicketService ticketService;
    private final SeatService seatService;
    private final TripRepository tripRepository;

    @GetMapping("/book")
    public String showBookingForm(@RequestParam(required = false) Long tripId, Model model) {
        if (tripId == null) {
            return "redirect:/trips/search";
        }
        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip == null) {
            model.addAttribute("error", "Không tìm thấy chuyến xe");
            return "passenger/booking-form";
        }
        model.addAttribute("trip", trip);
        model.addAttribute("seats", seatService.getAvailableSeats(tripId));
        model.addAttribute("bookingDTO", new BookingRequestDTO());
        return "passenger/booking-form";
    }

    @PostMapping("/book")
    public String bookTicket(@Valid @ModelAttribute BookingRequestDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            Trip trip = tripRepository.findById(dto.getTripId()).orElse(null);
            if (trip != null) {
                model.addAttribute("trip", trip);
                model.addAttribute("seats", seatService.getAvailableSeats(dto.getTripId()));
            }
            return "passenger/booking-form";
        }

        try {
            String ticketCode = bookingService.processBooking(dto);
            model.addAttribute("message", "Đặt vé thành công! Mã vé của bạn là: " + ticketCode);
        } catch (Exception e) {
            // Không hiển thị lỗi SQL/stack trace ra UI
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = "Có lỗi xảy ra. Vui lòng thử lại.";
            }
            model.addAttribute("error", msg);
        }
        return "passenger/booking-result";
    }

    // CORE-07: Trang tra cứu vé
    @GetMapping("/search")
    public String searchTicket(@RequestParam(required = false) String code,
                               @RequestParam(required = false) String phone,
                               Model model) {
        if (code != null && phone != null) {
            try {
                TicketDetailDTO ticketDetail = ticketService.getTicketDetail(code, phone);
                model.addAttribute("ticket", ticketDetail);
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "passenger/search-ticket";
    }

    // CORE-09: Khách hàng yêu cầu hủy vé
    @PostMapping("/cancel")
    public String cancelTicket(@RequestParam Long ticketId,
                               @RequestParam String phone,
                               Model model) {
        try {
            ticketService.cancelProcess(ticketId, phone);
            model.addAttribute("message", "Hủy vé thành công! Ghế đã được giải phóng.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "passenger/cancel-result";
    }
}

