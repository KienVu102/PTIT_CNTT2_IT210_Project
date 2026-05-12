package com.example.project.service;

import com.example.project.dto.TripDTO;
import com.example.project.dto.TripSearchDTO;
import com.example.project.entity.Bus;
import com.example.project.entity.Location;
import com.example.project.entity.Route;
import com.example.project.entity.Seat;
import com.example.project.entity.Trip;
import com.example.project.enums.SeatStatus;
import com.example.project.repository.BusRepository;
import com.example.project.repository.LocationRepository;
import com.example.project.repository.RouteRepository;
import com.example.project.repository.SeatRepository;
import com.example.project.repository.TicketRepository;
import com.example.project.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final SeatRepository seatRepository;
    private final RouteRepository routeRepository;
    private final LocationRepository locationRepository;
    private final BusRepository busRepository;
    private final TicketRepository ticketRepository;

    public List<TripSearchDTO> searchTrips(Long fromId, Long toId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        List<Trip> trips = tripRepository.findTrips(fromId, toId, start, end);
        return trips.stream()
                .filter(t -> t.getDepartureTime().isAfter(now))
                .map(this::convertToSearchDTO)
                .collect(Collectors.toList());
    }

    // Huong 3 Mo rong: tim tat ca khung gio cua 1 tuyen duong
    public List<TripSearchDTO> searchTripsByRoute(Long fromId, Long toId) {
        LocalDateTime now = LocalDateTime.now();
        List<Trip> trips = tripRepository.findTripsByRoute(fromId, toId);
        return trips.stream()
                .filter(t -> t.getDepartureTime().isAfter(now))
                .map(this::convertToSearchDTO)
                .collect(Collectors.toList());
    }

    public List<TripSearchDTO> getAllTrips() {
        return tripRepository.findAll().stream()
                // Sap xep theo thoi gian tao (cu -> moi), fallback theo ID neu du lieu cu null createdAt.
                .sorted(Comparator
                        .comparing(Trip::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Trip::getId, Comparator.nullsFirst(Comparator.naturalOrder()))
                )
                .map(this::convertToSearchDTO)
                .collect(Collectors.toList());
    }

    public Page<TripSearchDTO> getPaginatedTrips(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return tripRepository.findAll(pageable).map(this::convertToSearchDTO);
    }

    @Transactional
    public void createTrip(TripDTO dto) {
        Route route = resolveRoute(dto);
        Bus bus = resolveBus(dto.getBusId());
        LocalDateTime departureTime = validateDepartureTime(dto.getDepartureTime());
        
        if (departureTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Không thể tạo chuyến xe với thời gian khởi hành trong quá khứ");
        }
        
        validateBusDepartureConflict(bus.getId(), departureTime, null);

        Trip trip = new Trip();
        trip.setRoute(route);
        trip.setBus(bus);
        trip.setDepartureTime(departureTime);
        trip.setPrice(dto.getPrice());

        Trip savedTrip = tripRepository.save(trip);

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= bus.getTotalSeats(); i++) {
            String seatNumber = (i < 10 ? "A0" : "A") + i;
            seats.add(new Seat(null, savedTrip, seatNumber, SeatStatus.AVAILABLE));
        }
        seatRepository.saveAll(seats);
    }

    @Transactional
    public void deleteTrip(Long id) {
        if (ticketRepository.existsByTrip_Id(id)) {
            throw new RuntimeException("Chuyến xe này đã có vé, không thể xóa!");
        }
        List<Seat> seats = seatRepository.findByTripId(id);
        seatRepository.deleteAll(seats);
        tripRepository.deleteById(id);
    }

    @Transactional
    public void updateTrip(Long tripId, TripDTO dto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay chuyen xe"));

        boolean hasTickets = ticketRepository.existsByTrip_Id(tripId);

        Route newRoute = resolveRouteForUpdate(dto, trip, hasTickets);
        Bus newBus = resolveBusForUpdate(dto, trip, hasTickets);
        LocalDateTime departureTime = validateDepartureTime(dto.getDepartureTime());

        boolean routeChanged = trip.getRoute() == null || !trip.getRoute().getId().equals(newRoute.getId());
        boolean busChanged = trip.getBus() == null || !trip.getBus().getId().equals(newBus.getId());

        if (hasTickets && (routeChanged || busChanged)) {
            throw new RuntimeException("Chuyen xe da co ve. Chi duoc sua thoi gian khoi hanh va gia ve.");
        }

        validateBusDepartureConflict(newBus.getId(), departureTime, tripId);

        trip.setRoute(newRoute);
        trip.setBus(newBus);
        trip.setDepartureTime(departureTime);
        trip.setPrice(dto.getPrice());

        Trip saved = tripRepository.save(trip);

        if (!hasTickets) {
            List<Seat> currentSeats = seatRepository.findByTripId(tripId);
            int desiredSeats = newBus.getTotalSeats();
            boolean seatCountMismatch = currentSeats.size() != desiredSeats;

            if (busChanged || seatCountMismatch) {
                seatRepository.deleteAll(currentSeats);

                List<Seat> seats = new ArrayList<>();
                for (int i = 1; i <= desiredSeats; i++) {
                    String seatNumber = (i < 10 ? "A0" : "A") + i;
                    seats.add(new Seat(null, saved, seatNumber, SeatStatus.AVAILABLE));
                }
                seatRepository.saveAll(seats);
            }
        }
    }

    public boolean hasTickets(Long tripId) {
        return ticketRepository.existsByTrip_Id(tripId);
    }

    private Route resolveRouteForUpdate(TripDTO dto, Trip currentTrip, boolean hasTickets) {
        boolean hasRouteInput = dto.getRouteId() != null
                || dto.getFromLocationId() != null
                || dto.getToLocationId() != null;

        if (hasRouteInput) {
            return resolveRoute(dto);
        }

        if (hasTickets && currentTrip.getRoute() != null) {
            return currentTrip.getRoute();
        }

        throw new RuntimeException("Vui long chon diem di va diem den");
    }

    private Bus resolveBusForUpdate(TripDTO dto, Trip currentTrip, boolean hasTickets) {
        if (dto.getBusId() != null) {
            return resolveBus(dto.getBusId());
        }

        if (hasTickets && currentTrip.getBus() != null) {
            return currentTrip.getBus();
        }

        throw new RuntimeException("Vui long chon xe");
    }

    private Route resolveRoute(TripDTO dto) {
        if (dto.getRouteId() != null) {
            return routeRepository.findById(dto.getRouteId())
                    .orElseThrow(() -> new RuntimeException("Khong tim thay tuyen duong"));
        }

        Long fromId = dto.getFromLocationId();
        Long toId = dto.getToLocationId();

        if (fromId == null || toId == null) {
            throw new RuntimeException("Vui long chon diem di va diem den");
        }
        if (fromId.equals(toId)) {
            throw new RuntimeException("Diem di va diem den phai khac nhau");
        }

        Location fromLocation = locationRepository.findById(fromId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay diem di"));
        Location toLocation = locationRepository.findById(toId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay diem den"));

        return routeRepository.findByFromLocation_IdAndToLocation_Id(fromId, toId)
                .orElseGet(() -> routeRepository.save(new Route(null, fromLocation, toLocation, null)));
    }

    private Bus resolveBus(Long busId) {
        if (busId == null) {
            throw new RuntimeException("Vui long chon xe");
        }
        return busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay xe"));
    }

    private LocalDateTime validateDepartureTime(LocalDateTime departureTime) {
        if (departureTime == null) {
            throw new RuntimeException("Vui long chon thoi gian khoi hanh");
        }
        return departureTime;
    }

    private void validateBusDepartureConflict(Long busId, LocalDateTime departureTime, Long excludedTripId) {
        boolean conflict = (excludedTripId == null)
                ? tripRepository.existsByBus_IdAndDepartureTime(busId, departureTime)
                : tripRepository.existsByBus_IdAndDepartureTimeAndIdNot(busId, departureTime, excludedTripId);

        if (conflict) {
            throw new RuntimeException("Xe da co chuyen trung khung gio trong ngay nay. Vui long chon gio khac hoac xe khac.");
        }
    }

    private TripSearchDTO convertToSearchDTO(Trip trip) {
        long available = seatRepository.findByTripId(trip.getId())
                .stream()
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                .count();

        return new TripSearchDTO(
                trip.getId(),
                trip.getRoute().getFromLocation().getName(),
                trip.getRoute().getToLocation().getName(),
                trip.getDepartureTime(),
                trip.getPrice(),
                trip.getBus().getPlateNumber(),
                trip.getBus().getBusType(),
                (int) available
        );
    }

    /**
     * Tự động xóa các chuyến xe đã qua thời gian khởi hành.
     * Chạy mỗi 30 phút. Chỉ xóa chuyến xe chưa có vé nào được đặt.
     */
    @Transactional
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 1800000) // 30 phút
    public void cleanupExpiredTrips() {
        List<Trip> expiredTrips = tripRepository.findByDepartureTimeBefore(LocalDateTime.now());
        for (Trip trip : expiredTrips) {
            if (!ticketRepository.existsByTrip_Id(trip.getId())) {
                List<Seat> seats = seatRepository.findByTripId(trip.getId());
                seatRepository.deleteAll(seats);
                tripRepository.delete(trip);
            }
        }
    }
}
