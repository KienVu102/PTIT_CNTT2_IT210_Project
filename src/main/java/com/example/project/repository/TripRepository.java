package com.example.project.repository;

import com.example.project.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    boolean existsByBus_Id(Long busId);

    boolean existsByBus_IdAndDepartureTime(Long busId, LocalDateTime departureTime);

    boolean existsByBus_IdAndDepartureTimeAndIdNot(Long busId, LocalDateTime departureTime, Long id);

    // CORE-05: Tim kiem chuyen xe theo diem di, den, ngay
    @Query("SELECT t FROM Trip t " +
           "JOIN FETCH t.route r " +
           "JOIN FETCH r.fromLocation fl " +
           "JOIN FETCH r.toLocation tl " +
           "JOIN FETCH t.bus b " +
           "WHERE fl.id = :fromId AND tl.id = :toId " +
           "AND t.departureTime >= :startOfDay AND t.departureTime < :endOfDay " +
           "ORDER BY t.departureTime ASC")
    List<Trip> findTrips(@Param("fromId") Long fromId,
                         @Param("toId") Long toId,
                         @Param("startOfDay") LocalDateTime startOfDay,
                         @Param("endOfDay") LocalDateTime endOfDay);

    // Huong 3 Mo rong: Tim kiem chuyen xe theo tuyen duong (khong can ngay)
    @Query("SELECT t FROM Trip t " +
           "JOIN FETCH t.route r " +
           "JOIN FETCH r.fromLocation fl " +
           "JOIN FETCH r.toLocation tl " +
           "JOIN FETCH t.bus b " +
           "WHERE fl.id = :fromId AND tl.id = :toId " +
           "ORDER BY t.departureTime ASC")
    List<Trip> findTripsByRoute(@Param("fromId") Long fromId,
                                @Param("toId") Long toId);

    // Tìm các chuyến xe đã qua thời gian khởi hành
    List<Trip> findByDepartureTimeBefore(LocalDateTime dateTime);
}
