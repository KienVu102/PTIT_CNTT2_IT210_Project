package com.example.project.repository;
import com.example.project.entity.Ticket;
import com.example.project.enums.TicketStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(TicketStatus status);
    boolean existsByTrip_Id(Long tripId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t JOIN FETCH t.seat " +
           "JOIN FETCH t.trip tr JOIN FETCH tr.route r " +
           "JOIN FETCH r.fromLocation JOIN FETCH r.toLocation " +
           "WHERE t.id = :id")
    Optional<Ticket> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT t FROM Ticket t " +
           "JOIN FETCH t.trip tr JOIN FETCH tr.bus JOIN FETCH tr.route r " +
           "JOIN FETCH r.fromLocation JOIN FETCH r.toLocation " +
           "JOIN FETCH t.seat " +
           "WHERE t.ticketCode = :code AND t.customerPhone = :phone")
    Optional<Ticket> findByTicketCodeAndCustomerPhone(@Param("code") String code, @Param("phone") String phone);
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Seat s SET s.status = com.example.project.enums.SeatStatus.AVAILABLE WHERE s.id IN (SELECT t.seat.id FROM Ticket t WHERE t.status = 'PENDING' AND t.bookingTime < :threshold)")
    int freeExpiredSeats(@Param("threshold") LocalDateTime threshold);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Ticket t SET t.status = 'CANCELLED' WHERE t.status = 'PENDING' AND t.bookingTime < :threshold")
    int cancelExpiredTickets(@Param("threshold") LocalDateTime threshold);
    List<Ticket> findByCustomerPhoneOrderByBookingTimeDesc(String phone);
    @Query("SELECT SUM(t.totalPrice) FROM Ticket t WHERE t.status = 'PAID'")
    Double getTotalRevenue();
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status")
    Long countByStatus(@Param("status") TicketStatus status);
    @Query("SELECT r.fromLocation.name, r.toLocation.name, COUNT(t) FROM Ticket t " +
           "JOIN t.trip tr JOIN tr.route r " +
           "WHERE t.status = 'PAID' " +
           "GROUP BY r.id " +
           "ORDER BY COUNT(t) DESC")
    List<Object[]> getTopRoutes();
    List<Ticket> findAllByOrderByBookingTimeDesc();
    org.springframework.data.domain.Page<Ticket> findAllByOrderByBookingTimeDesc(org.springframework.data.domain.Pageable pageable);
}
