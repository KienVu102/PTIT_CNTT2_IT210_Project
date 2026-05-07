package com.example.project.repository;

import com.example.project.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    // CORE-05: Tìm kiếm chuyến xe theo điểm đi, đến, ngày
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
}
