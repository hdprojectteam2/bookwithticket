package com.example.bookwithticket.domain.reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.schedule s JOIN FETCH s.performance JOIN FETCH r.seat WHERE r.memberId = :memberId ORDER BY r.id DESC")
    List<Reservation> findByMemberIdOrderByIdDesc(@Param("memberId") Long memberId);
    Optional<Reservation> findByIdAndMemberId(Long id, Long memberId);
    Optional<Reservation> findByScheduleIdAndSeatIdAndStatus(Long scheduleId, Long seatId, ReservationStatus status);
    Optional<Reservation> findFirstByScheduleIdAndSeatIdAndStatusOrderByIdDesc(Long scheduleId, Long seatId, ReservationStatus status);
    List<Reservation> findByScheduleIdAndStatusIn(Long scheduleId, List<ReservationStatus> statuses);
    
    // 만료된 HELD 예약 조회 (스케줄러용)
    List<Reservation> findByStatusAndHoldExpiresAtBefore(ReservationStatus status, LocalDateTime dateTime);
    
    // 스케줄 ID에 연관된 모든 예매 삭제
    void deleteByScheduleId(Long scheduleId);
}
