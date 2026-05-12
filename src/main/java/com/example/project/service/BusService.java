package com.example.project.service;
import com.example.project.dto.BusDTO;
import com.example.project.entity.Bus;
import com.example.project.repository.BusRepository;
import com.example.project.repository.TicketRepository;
import com.example.project.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusService {
    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;
    public List<BusDTO> getAllBuses() {
        return busRepository.findAll().stream()
                .map(bus -> new BusDTO(bus.getId(), bus.getPlateNumber(), bus.getBusType(),
                        bus.getTotalSeats(), bus.getCompany(), bus.getDriverName()))
                .collect(Collectors.toList());
    }
    public void createBus(BusDTO dto) {
        Bus bus = new Bus();
        bus.setPlateNumber(dto.getPlateNumber());
        bus.setBusType(dto.getBusType());
        bus.setTotalSeats(dto.getTotalSeats());
        bus.setCompany(dto.getCompany());
        bus.setDriverName(dto.getDriverName());
        busRepository.save(bus);
    }
    public BusDTO getBusById(Long id) {
        return busRepository.findById(id)
                .map(bus -> new BusDTO(bus.getId(), bus.getPlateNumber(), bus.getBusType(),
                        bus.getTotalSeats(), bus.getCompany(), bus.getDriverName()))
                .orElseThrow(() -> new RuntimeException("Bus not found"));
    }
    public void updateBus(Long id, BusDTO dto) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bus not found"));
        bus.setPlateNumber(dto.getPlateNumber());
        bus.setBusType(dto.getBusType());
        bus.setTotalSeats(dto.getTotalSeats());
        bus.setCompany(dto.getCompany());
        bus.setDriverName(dto.getDriverName());
        busRepository.save(bus);
    }
    public void deleteBus(Long id) {
        // Kiểm tra xe có đang được sử dụng trong chuyến xe nào không
        if (tripRepository.existsByBus_Id(id)) {
            // Kiểm tra trong các chuyến xe đó có vé nào không
            var trips = tripRepository.findAll().stream()
                    .filter(t -> t.getBus().getId().equals(id))
                    .collect(Collectors.toList());
            for (var trip : trips) {
                if (ticketRepository.existsByTrip_Id(trip.getId())) {
                    throw new RuntimeException("Xe buýt này đang có chuyến xe đã được đặt vé, không thể xóa!");
                }
            }
            // Nếu không có vé nào, vẫn không cho xóa vì xe đang được gán cho chuyến xe
            throw new RuntimeException("Xe buýt này đang được sử dụng trong chuyến xe, không thể xóa!");
        }
        busRepository.deleteById(id);
    }
    public boolean isPlateNumberExists(String plateNumber) {
        return busRepository.existsByPlateNumber(plateNumber);
    }
}
