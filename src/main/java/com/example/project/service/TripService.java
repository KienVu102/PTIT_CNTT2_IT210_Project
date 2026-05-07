package com.example.project.service;

import com.example.project.dto.TripDTO;
import com.example.project.dto.TripSearchDTO;
import com.example.project.entity.Bus;
import com.example.project.entity.Route;
import com.example.project.entity.Seat;
import com.example.project.entity.Trip;
import com.example.project.enums.SeatStatus;
import com.example.project.repository.BusRepository;
import com.example.project.repository.RouteRepository;
import com.example.project.repository.SeatRepository;
import com.example.project.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {
    
    private final TripRepository tripRepository;
    private final SeatRepository seatRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    
    public List<TripSearchDTO> searchTrips(Long fromId, Long toId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<Trip> trips = tripRepository.findTrips(fromId, toId, start, end);
        return trips.stream().map(this::convertToSearchDTO).collect(Collectors.toList());
    }
    
    public List<TripSearchDTO> getAllTrips() {
        return tripRepository.findAll().stream()
                .sorted((a, b) -> b.getDepartureTime().compareTo(a.getDepartureTime()))
                .map(this::convertToSearchDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void createTrip(TripDTO dto) {
        Route route = routeRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));
        Bus bus = busRepository.findById(dto.getBusId())
                .orElseThrow(() -> new RuntimeException("Bus not found"));
        
        Trip trip = new Trip();
        trip.setRoute(route);
        trip.setBus(bus);
        trip.setDepartureTime(dto.getDepartureTime());
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
        List<Seat> seats = seatRepository.findByTripId(id);
        seatRepository.deleteAll(seats);
        tripRepository.deleteById(id);
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
}