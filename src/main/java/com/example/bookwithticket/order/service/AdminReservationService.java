package com.example.bookwithticket.order.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.domain.performance.PerformanceSchedule;
import com.example.bookwithticket.domain.reservation.ReservationRepository;
import com.example.bookwithticket.domain.reservation.Seat;
import com.example.bookwithticket.order.dto.AdminReservationResponse;

@Service
@Transactional(readOnly = true)
public class AdminReservationService {

	private final ReservationRepository reservationRepository;

	public AdminReservationService(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	public List<AdminReservationResponse> findReservations() {

		return reservationRepository.findAll().stream().sorted((a, b) -> Long.compare(b.getId(), a.getId()))
				.map(reservation -> {

					PerformanceSchedule schedule = reservation.getSchedule();
					Seat seat = reservation.getSeat();
					return new AdminReservationResponse(
							reservation.getId(),
							reservation.getMemberId(),
							reservation.getCreatedAt(),
							schedule.getPerformance().getTitle(),
							schedule.getPerformanceTime(),
							seat.getSeatNumber(),
							reservation.getTotalPrice(),
							reservation.getStatus().name());
				}).toList();
	}
}