package com.example.project.service;

import com.example.project.dto.ProfileDTO;
import com.example.project.dto.RegisterDTO;
import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // CORE-01: Đăng ký tài khoản Hành khách, mật khẩu BCrypt
    @Transactional
    public void register(RegisterDTO dto) {
        String username = dto.getUsername() == null ? null : dto.getUsername().trim();
        String phone = dto.getPhone() == null ? null : dto.getPhone().trim();
        String email = dto.getEmail() == null ? null : dto.getEmail().trim().toLowerCase();

        if (username != null && userRepository.findByUsername(username) != null) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Email đã tồn tại");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.PASSENGER);
        user.setFullName(dto.getFullName());
        user.setPhone(phone);
        user.setEmail(email);
        userRepository.save(user);
    }

    // CORE-03: Lấy thông tin profile
    public User getProfile(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("Không tìm thấy người dùng");
        return user;
    }

    // CORE-03: Cập nhật profile
    @Transactional
    public void updateProfile(String username, ProfileDTO dto) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("Không tìm thấy người dùng");
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        userRepository.save(user);
    }
}

