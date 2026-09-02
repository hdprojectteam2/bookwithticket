package com.example.bookwithticket.order.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.domain.performance.PerformanceSchedule;
import com.example.bookwithticket.domain.reservation.ReservationRepository;
import com.example.bookwithticket.domain.reservation.Seat;
import com.example.bookwithticket.order.dto.AdminReservationResponse;
import com.example.bookwithticket.payment.entity.PaymentEntity;
import com.example.bookwithticket.payment.entity.PaymentStatus;
import com.example.bookwithticket.payment.repository.PaymentRepository;

@Service
@Transactional(readOnly = true)
public class AdminReservationService {

	private final ReservationRepository reservationRepository;
	private final PaymentRepository paymentRepository;

	public AdminReservationService(ReservationRepository reservationRepository, PaymentRepository paymentRepository) {
		this.reservationRepository = reservationRepository;
		this.paymentRepository = paymentRepository;
	}

	public List<AdminReservationResponse> findReservations() {

		return reservationRepository.findAll().stream().sorted((a, b) -> Long.compare(b.getId(), a.getId()))
				.map(reservation -> {

					PerformanceSchedule schedule = reservation.getSchedule();
					Seat seat = reservation.getSeat();
					PaymentEntity payment = paymentRepository.findFirstByReservationIdAndStatusInOrderByCreatedAtDesc(
							reservation.getId(), List.of(PaymentStatus.DONE, PaymentStatus.CANCELED)).orElse(null);

					LocalDateTime createdAt = payment != null ? payment.getCreatedAt() : null;
					return new AdminReservationResponse(reservation.getId(), reservation.getReservationNumber(),
							reservation.getMemberId(), createdAt, schedule.getPerformance().getTitle(),
							schedule.getPerformanceTime(), seat.getSeatNumber(), reservation.getTotalPrice(),
							reservation.getStatus().name());
				}).toList();
	}
}