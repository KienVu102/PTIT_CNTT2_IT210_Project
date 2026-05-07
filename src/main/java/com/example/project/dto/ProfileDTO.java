package com.example.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileDTO {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    private String phone;
    @Email(message = "Email không hợp lệ")
    private String email;
}
