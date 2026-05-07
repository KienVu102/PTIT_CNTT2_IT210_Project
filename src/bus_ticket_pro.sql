DROP DATABASE IF EXISTS bus_ticket_pro;
CREATE DATABASE bus_ticket_pro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bus_ticket_pro;

CREATE TABLE locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE buses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plate_number VARCHAR(255) NOT NULL UNIQUE,
    bus_type VARCHAR(255),
    total_seats INT,
    company VARCHAR(255),
    driver_name VARCHAR(255)
);

CREATE TABLE routes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_location_id BIGINT NOT NULL,
    to_location_id BIGINT NOT NULL,
    distance DOUBLE,
    FOREIGN KEY (from_location_id) REFERENCES locations(id),
    FOREIGN KEY (to_location_id) REFERENCES locations(id)
);

CREATE TABLE trips (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    route_id BIGINT NOT NULL,
    bus_id BIGINT NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    price DOUBLE NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes(id),
    FOREIGN KEY (bus_id) REFERENCES buses(id)
);

CREATE TABLE seats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trip_id BIGINT NOT NULL,
    seat_number VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'PENDING', 'BOOKED')),
    FOREIGN KEY (trip_id) REFERENCES trips(id)
);

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) CHECK (role IN ('PASSENGER', 'STAFF', 'ADMIN')),
    full_name VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255)
);

CREATE TABLE tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_code VARCHAR(255) NOT NULL UNIQUE,
    customer_name VARCHAR(255),
    customer_phone VARCHAR(255),
    customer_email VARCHAR(255),
    trip_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL UNIQUE,
    total_price DOUBLE,
    status VARCHAR(20) CHECK (status IN ('PENDING', 'PAID', 'CANCELLED')),
    booking_time TIMESTAMP,
    FOREIGN KEY (trip_id) REFERENCES trips(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id)
);

CREATE INDEX idx_seats_trip_id ON seats(trip_id);
CREATE INDEX idx_tickets_trip_id ON tickets(trip_id);
CREATE INDEX idx_tickets_seat_id ON tickets(seat_id);
CREATE INDEX idx_routes_from_location ON routes(from_location_id);
CREATE INDEX idx_routes_to_location ON routes(to_location_id);
CREATE INDEX idx_trips_route_id ON trips(route_id);
CREATE INDEX idx_trips_bus_id ON trips(bus_id);
CREATE INDEX idx_trips_departure_time ON trips(departure_time);

INSERT INTO locations (name) VALUES 
('Hà Nội'),
('TP. Hồ Chí Minh'),
('Đà Nẵng'),
('Hải Phòng'),
('Nha Trang');

INSERT INTO buses (plate_number, bus_type, total_seats, company, driver_name) VALUES
('29A-12345', 'Ghế ngồi', 45, 'Hà Nội Transport', 'Nguyễn Văn A'),
('29B-67890', 'Giường nằm', 32, 'Sài Gòn Bus', 'Trần Văn B'),
('29C-11111', 'Limousine', 20, 'Đà Nẵng Travel', 'Lê Văn C');

INSERT INTO routes (from_location_id, to_location_id, distance) VALUES
(1, 2, 1600), -- Hà Nội -> TP.HCM
(2, 1, 1600), -- TP.HCM -> Hà Nội
(1, 3, 800),  -- Hà Nội -> Đà Nẵng
(3, 1, 800),  -- Đà Nẵng -> Hà Nội
(2, 3, 950);  -- TP.HCM -> Đà Nẵng

-- Trips
INSERT INTO trips (route_id, bus_id, departure_time, price) VALUES 
(1, 1, '2026-05-10 08:00:00', 350000), -- Hà Nội -> TP.HCM
(2, 2, '2026-05-10 09:00:00', 350000), -- TP.HCM -> Hà Nội
(3, 3, '2026-05-11 07:30:00', 280000), -- Hà Nội -> Đà Nẵng
(4, 1, '2026-05-11 14:00:00', 280000), -- Đà Nẵng -> Hà Nội
(5, 2, '2026-05-12 10:00:00', 320000); -- TP.HCM -> Đà Nẵng

-- Seats for trip 1 (Hà Nội -> TP.HCM)
INSERT INTO seats (trip_id, seat_number, status) VALUES 
(1, 'A1', 'AVAILABLE'), (1, 'A2', 'AVAILABLE'), (1, 'A3', 'AVAILABLE'),
(1, 'B1', 'PENDING'), (1, 'B2', 'AVAILABLE'), (1, 'B3', 'AVAILABLE'),
(1, 'C1', 'BOOKED'), (1, 'C2', 'AVAILABLE'), (1, 'C3', 'AVAILABLE');

-- Seats for trip 2 (TP.HCM -> Hà Nội)
INSERT INTO seats (trip_id, seat_number, status) VALUES 
(2, 'A1', 'AVAILABLE'), (2, 'A2', 'PENDING'), (2, 'A3', 'AVAILABLE'),
(2, 'B1', 'AVAILABLE'), (2, 'B2', 'BOOKED'), (2, 'B3', 'AVAILABLE');

-- Seats for trip 3 (Hà Nội -> Đà Nẵng)
INSERT INTO seats (trip_id, seat_number, status) VALUES 
(3, 'A1', 'AVAILABLE'), (3, 'A2', 'AVAILABLE'), (3, 'A3', 'AVAILABLE'),
(3, 'B1', 'AVAILABLE'), (3, 'B2', 'PENDING'), (3, 'B3', 'AVAILABLE');

-- Users
INSERT INTO users (username, password_hash, role, full_name, phone, email) VALUES 
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', 'Admin User', '0901234567', 'admin@example.com'),
('staff1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Staff Member', '0909876543', 'staff@example.com'),
('user1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'PASSENGER', 'John Doe', '0912345678', 'user1@example.com'),
('user2', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'PASSENGER', 'Jane Smith', '0923456789', 'user2@example.com');

-- Tickets
INSERT INTO tickets (ticket_code, customer_name, customer_phone, customer_email, trip_id, seat_id, total_price, status, booking_time) VALUES 
('TK001', 'John Doe', '0912345678', 'user1@example.com', 1, 4, 350000, 'PENDING', '2026-05-07 10:30:00'),
('TK002', 'Jane Smith', '0923456789', 'user2@example.com', 2, 5, 350000, 'PAID', '2026-05-07 11:00:00'),
('TK003', 'Nguyễn Văn A', '0901234567', 'customer1@example.com', 3, 8, 280000, 'PENDING', '2026-05-07 12:15:00');

