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

                Trip t1 = tripRepo.save(new Trip(null, r1, bus1, tomorrow, 150000.0));
                Trip t2 = tripRepo.save(new Trip(null, r1, bus2, tomorrow2, 200000.0));
                Trip t3 = tripRepo.save(new Trip(null, r2, bus3, tomorrow, 120000.0));
                Trip t4 = tripRepo.save(new Trip(null, r4, bus2, dayAfter, 350000.0));

                // Seed Seats cho mỗi chuyến
                seedSeats(seatRepo, t1, bus1.getTotalSeats());
                seedSeats(seatRepo, t2, bus2.getTotalSeats());
                seedSeats(seatRepo, t3, bus3.getTotalSeats());
                seedSeats(seatRepo, t4, bus2.getTotalSeats());
            }

            // Seed Users (CORE-01 - mật khẩu BCrypt)
            if (userRepo.count() == 0) {
                User admin = new User(null, "admin", passwordEncoder.encode("admin123"), Role.ADMIN, "Quản trị viên", "0900000001", "admin@busticket.com");
                User staff = new User(null, "staff", passwordEncoder.encode("staff123"), Role.STAFF, "Nhân viên bán vé", "0900000002", "staff@busticket.com");
                User passenger = new User(null, "passenger", passwordEncoder.encode("pass123"), Role.PASSENGER, "Hành khách Demo", "0900000003", "passenger@busticket.com");
                userRepo.saveAll(Arrays.asList(admin, staff, passenger));
                System.out.println("=== SEED USERS ===");
                System.out.println("admin / admin123 (ADMIN)");
                System.out.println("staff / staff123 (STAFF)");
                System.out.println("passenger / pass123 (PASSENGER)");
            }
        };
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
