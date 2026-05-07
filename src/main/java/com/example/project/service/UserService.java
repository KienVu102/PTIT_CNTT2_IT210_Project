package com.example.project.service;

import com.example.project.dto.ProfileDTO;
import com.example.project.dto.RegisterDTO;
import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // CORE-01: Đăng ký tài khoản Hành khách, mật khẩu BCrypt
    @Transactional
    public void register(RegisterDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()) != null) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        // BCrypt hash mật khẩu - CORE-01
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.PASSENGER);
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
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
