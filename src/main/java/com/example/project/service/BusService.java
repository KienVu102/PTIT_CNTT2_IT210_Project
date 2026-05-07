package com.example.project.service;
import com.example.project.dto.BusDTO;
import com.example.project.entity.Bus;
import com.example.project.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusService {
    private final BusRepository busRepository;
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
        busRepository.deleteById(id);
    }
}
