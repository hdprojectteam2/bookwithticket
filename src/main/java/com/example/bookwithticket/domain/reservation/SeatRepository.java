package com.example.bookwithticket.domain.reservation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScheduleIdOrderByIdAsc(Long scheduleId);
    List<Seat> findByScheduleIdOrderBySeatNumberAsc(Long scheduleId);
    Optional<Seat> findByScheduleIdAndSeatNumber(Long scheduleId, String seatNumber);
}
