package com.example.bookwithticket.history.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookwithticket.history.dto.PerformanceHistoryDto;
import com.example.bookwithticket.payment.entity.PaymentEntity;
import com.example.bookwithticket.payment.entity.PaymentStatus;
import com.example.bookwithticket.payment.repository.PaymentRepository;
import com.example.bookwithticket.performance.entity.PerformanceEntity;
import com.example.bookwithticket.performance.entity.PerformanceScheduleEntity;
import com.example.bookwithticket.refund.entity.RefundEntity;
import com.example.bookwithticket.refund.entity.RefundStatus;
import com.example.bookwithticket.refund.repository.RefundRepository;
import com.example.bookwithticket.reservation.entity.ReservationEntity;
import com.example.bookwithticket.reservation.entity.ReservationStatus;
import com.example.bookwithticket.reservation.repository.ReservationRepository;
import com.example.bookwithticket.reservation.repository.ReservationSeatRepository;

@Service
@Transactional(readOnly = true)
public class PerformanceHistoryServiceImpl implements PerformanceHistoryService {

	private final ReservationRepository reservationRepository;
	private final PaymentRepository paymentRepository;
	private final RefundRepository refundRepository;
	private final ReservationSeatRepository reservationSeatRepository;
	
	public PerformanceHistoryServiceImpl(ReservationRepository reservationRepository,
			PaymentRepository paymentRepository, RefundRepository refundRepository, ReservationSeatRepository reservationSeatRepository) {
		this.reservationRepository = reservationRepository;
		this.paymentRepository = paymentRepository;
		this.refundRepository = refundRepository;
		this.reservationSeatRepository = reservationSeatRepository;
	}

	@Override
	public List<PerformanceHistoryDto> findPerformanceHistory(Long memberId) {

		List<ReservationStatus> visibleStatus = List.of(ReservationStatus.CONFIRMED, ReservationStatus.CANCELLED);

		List<ReservationEntity> reservations = reservationRepository
				.findByMemberIdAndStatusInOrderByCreatedAtDesc(memberId, visibleStatus);
		return reservations.stream().map(this::toDto).toList();
	}

	private PerformanceHistoryDto toDto(ReservationEntity reservation) {

		PerformanceScheduleEntity schedule = reservation.getPerformanceSchedule();

		PerformanceEntity performance = schedule.getPerformance();

		List<String> seatNumbers =
		        reservationSeatRepository
		                .findByReservationId(reservation.getId())
		                .stream()
		                .map(reservationSeat ->
		                        reservationSeat.getSeatNumber()
		                )
		                .toList();
		
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

		return new PerformanceHistoryDto(reservation.getReservationNumber(), performance.getTitle(),
				schedule.getStartAt(), performance.getPosterUrl(), performance.getVenue(), reservation.getTotalPrice(),
				convertReservationStatus(reservation.getStatus()), reservation.getStatus().name(), refundStatus,
				refundStatusCode, seatNumbers);
	}

	private String convertReservationStatus(ReservationStatus status) {
		return switch (status) {
		case PAYMENT_PENDING -> "결제 대기";
		case CONFIRMED -> "예매 완료";
		case CANCELLED -> "예매 취소";
		default -> "예매 상태 확인 중";
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
