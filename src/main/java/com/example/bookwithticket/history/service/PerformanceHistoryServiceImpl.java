package com.example.bookwithticket.history.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.domain.performance.Performance;
import com.example.bookwithticket.domain.performance.PerformanceSchedule;
import com.example.bookwithticket.domain.reservation.Reservation;
import com.example.bookwithticket.domain.reservation.ReservationRepository;
import com.example.bookwithticket.domain.reservation.ReservationStatus;
import com.example.bookwithticket.history.dto.PerformanceHistoryDto;
import com.example.bookwithticket.payment.entity.PaymentEntity;
import com.example.bookwithticket.payment.entity.PaymentStatus;
import com.example.bookwithticket.payment.repository.PaymentRepository;
import com.example.bookwithticket.refund.entity.RefundEntity;
import com.example.bookwithticket.refund.entity.RefundStatus;
import com.example.bookwithticket.refund.repository.RefundRepository;

@Service
@Transactional(readOnly = true)
public class PerformanceHistoryServiceImpl implements PerformanceHistoryService {

	private final ReservationRepository reservationRepository;
	private final PaymentRepository paymentRepository;
	private final RefundRepository refundRepository;

	public PerformanceHistoryServiceImpl(ReservationRepository reservationRepository,
			PaymentRepository paymentRepository, RefundRepository refundRepository) {
		this.reservationRepository = reservationRepository;
		this.paymentRepository = paymentRepository;
		this.refundRepository = refundRepository;
	}

	@Override
	public List<PerformanceHistoryDto> findPerformanceHistory(Long memberId) {

		return reservationRepository.findByMemberIdOrderByIdDesc(memberId).stream()
				.filter(reservation -> reservation.getStatus() == ReservationStatus.CONFIRMED
						|| reservation.getStatus() == ReservationStatus.CANCELLED)
				.map(this::toDto).toList();
	}

	private PerformanceHistoryDto toDto(Reservation reservation) {

		PerformanceSchedule schedule = reservation.getSchedule();

		Performance performance = schedule.getPerformance();

		String refundStatus = null;
		String refundStatusCode = null;

		Optional<PaymentEntity> paymentOptional = paymentRepository
				.findFirstByReservationIdAndStatusInOrderByCreatedAtDesc(reservation.getId(),
						List.of(PaymentStatus.DONE, PaymentStatus.CANCELED));

		if (paymentOptional.isPresent()) {

			PaymentEntity payment = paymentOptional.get();

			Optional<RefundEntity> refundOptional = refundRepository.findByPaymentId(payment.getId());

			if (refundOptional.isPresent()) {

				RefundEntity refund = refundOptional.get();

				refundStatus = convertRefundStatus(refund.getStatus());

				refundStatusCode = refund.getStatus().name();
			}
		}

		return new PerformanceHistoryDto("PERF_" + reservation.getId(), performance.getTitle(),
				schedule.getPerformanceTime(), performance.getPosterUrl(), performance.getVenue(),
				reservation.getSeat().getSeatNumber(), reservation.getTotalPrice(),
				convertReservationStatus(reservation.getStatus()), reservation.getStatus().name(), refundStatus,
				refundStatusCode);
	}

	private String convertReservationStatus(ReservationStatus status) {

		return switch (status) {
		case HELD -> "좌석 선점";
		case CONFIRMED -> "예매 완료";
		case CANCELLED -> "예매 취소";
		case EXPIRED -> "선점 만료";
		};
	}

	private String convertRefundStatus(RefundStatus status) {

		return switch (status) {
		case REQUESTED -> "환불 요청 중";
		case APPROVED -> "환불 처리 중";
		case REJECTED -> "환불 거절";
		case COMPLETED -> "환불 완료";
		case FAILED -> "환불 처리 실패";
		};
	}
}