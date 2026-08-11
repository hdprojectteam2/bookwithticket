package com.example.bookwithticket.domain.reservation;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    public ReservationScheduler(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    // 1분마다 만료된 HELD 좌석 자동 정리
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireHeldSeats() {
        List<Reservation> expired = reservationRepository.findByStatusAndHoldExpiresAtBefore(
                ReservationStatus.HELD,
                LocalDateTime.now()
        );

        for (Reservation reservation : expired) {
            reservation.expire();
            reservation.getSeat().updateStatus(SeatStatus.AVAILABLE);
        }
    }
}
