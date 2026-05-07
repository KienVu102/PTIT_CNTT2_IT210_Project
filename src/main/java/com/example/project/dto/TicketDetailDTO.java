package com.example.project.dto;

import com.example.project.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailDTO {
    // Thêm id để form hủy vé dùng đúng ticketId
    private Long id;
    private String ticketCode;
    private String customerName;
    private String customerPhone;

    // Thông tin xe
    private String plateNumber;
    private String busType;
    private String driverName;

    // Thông tin tuyến đường và thời gian
    private String fromLocation;
    private String toLocation;
    private LocalDateTime departureTime;

    // Thông tin ghế và thanh toán
    private String seatNumber;
    private Double totalPrice;
    private TicketStatus status;
}
