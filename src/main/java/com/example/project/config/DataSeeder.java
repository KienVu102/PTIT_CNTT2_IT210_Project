package com.example.project.config;

import com.example.project.entity.*;
import com.example.project.enums.Role;
import com.example.project.enums.SeatStatus;
import com.example.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedData(LocationRepository locationRepo,
                                      RouteRepository routeRepo,
                                      UserRepository userRepo,
                                      BusRepository busRepo,
                                      TripRepository tripRepo,
                                      SeatRepository seatRepo) {
        return args -> {
            // Seed Locations
            if (locationRepo.count() == 0) {
                Location haNoi   = locationRepo.save(new Location(null, "Hà Nội"));
                Location haiPhong = locationRepo.save(new Location(null, "Hải Phòng"));
                Location namDinh = locationRepo.save(new Location(null, "Nam Định"));
                Location hue     = locationRepo.save(new Location(null, "Huế"));
                Location danang  = locationRepo.save(new Location(null, "Đà Nẵng"));

                // Seed Routes
                Route r1 = routeRepo.save(new Route(null, haNoi, haiPhong, 120.0));
                Route r2 = routeRepo.save(new Route(null, haNoi, namDinh, 90.0));
                Route r3 = routeRepo.save(new Route(null, haiPhong, haNoi, 120.0));
                Route r4 = routeRepo.save(new Route(null, haNoi, hue, 680.0));
                Route r5 = routeRepo.save(new Route(null, haNoi, danang, 750.0));

                // Seed Buses
                Bus bus1 = busRepo.save(new Bus(null, "29A-12345", "Xe ghế 29 chỗ", 29, "Hoàng Long", "Nguyễn Văn A"));
                Bus bus2 = busRepo.save(new Bus(null, "30H-54321", "Xe giường 40 chỗ", 40, "Phuong Trang", "Trần Văn B"));
                Bus bus3 = busRepo.save(new Bus(null, "51B-99999", "Xe VIP 22 chỗ", 22, "Futa Bus", "Lê Thị C"));

                // Seed Trips (chuyến ngày hôm nay và ngày mai)
                LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(7).withMinute(0).withSecond(0);
                LocalDateTime tomorrow2 = LocalDateTime.now().plusDays(1).withHour(13).withMinute(30).withSecond(0);
                LocalDateTime dayAfter = LocalDateTime.now().plusDays(2).withHour(8).withMinute(0).withSecond(0);

                Trip t1 = tripRepo.save(new Trip(null, r1, bus1, tomorrow, 150000.0, null));
                Trip t2 = tripRepo.save(new Trip(null, r1, bus2, tomorrow2, 200000.0, null));
                Trip t3 = tripRepo.save(new Trip(null, r2, bus3, tomorrow, 120000.0, null));
                Trip t4 = tripRepo.save(new Trip(null, r4, bus2, dayAfter, 350000.0, null));

                // Seed Seats cho mỗi chuyến
                seedSeats(seatRepo, t1, bus1.getTotalSeats());
                seedSeats(seatRepo, t2, bus2.getTotalSeats());
                seedSeats(seatRepo, t3, bus3.getTotalSeats());
                seedSeats(seatRepo, t4, bus2.getTotalSeats());
            }

            // Seed Users (CORE-01 - mật khẩu BCrypt)
            // Kiểm tra từng user, nếu chưa có thì tạo, nếu có rồi thì cập nhật mật khẩu
            String defaultPassword = passwordEncoder.encode("password");
            seedOrUpdateUser(userRepo, "admin", defaultPassword, Role.ADMIN, "Admin User", "0901234567", "admin@example.com");
            seedOrUpdateUser(userRepo, "staff1", defaultPassword, Role.STAFF, "Staff Member", "0909876543", "staff@example.com");
            seedOrUpdateUser(userRepo, "user1", defaultPassword, Role.PASSENGER, "John Doe", "0912345678", "user1@example.com");
            seedOrUpdateUser(userRepo, "user2", defaultPassword, Role.PASSENGER, "Jane Smith", "0923456789", "user2@example.com");
            System.out.println("=== DEMO USERS (password: password) ===");
            System.out.println("admin (ADMIN) | staff1 (STAFF) | user1 (PASSENGER) | user2 (PASSENGER)");
        };
    }

    private void seedOrUpdateUser(UserRepository userRepo, String username, String encodedPassword,
                                   Role role, String fullName, String phone, String email) {
        User existing = userRepo.findByUsername(username);
        if (existing != null) {
            existing.setPasswordHash(encodedPassword);
            userRepo.save(existing);
        } else {
            userRepo.save(new User(null, username, encodedPassword, role, fullName, phone, email));
        }
    }

    private void seedSeats(SeatRepository seatRepo, Trip trip, int totalSeats) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= totalSeats; i++) {
            String seatNumber = (i < 10 ? "A0" : "A") + i;
            seats.add(new Seat(null, trip, seatNumber, SeatStatus.AVAILABLE));
        }
        seatRepo.saveAll(seats);
    }
}
