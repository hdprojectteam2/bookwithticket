	//redis
package com.example.bookwithticket.domain.reservation;

import com.example.bookwithticket.domain.performance.PerformanceSchedule;
import com.example.bookwithticket.domain.performance.PerformanceScheduleRepository;
import com.example.bookwithticket.global.exception.BusinessException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final PerformanceScheduleRepository scheduleRepository;
    private final StringRedisTemplate redisTemplate;

    public ReservationService(
            ReservationRepository reservationRepository,
            SeatRepository seatRepository,
            PerformanceScheduleRepository scheduleRepository,
            StringRedisTemplate redisTemplate) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.scheduleRepository = scheduleRepository;
        this.redisTemplate = redisTemplate;
    }
    //31. 조회 
    // N+1 문제 해결 
    @Transactional
    public List<SeatResponse> getSeats(Long scheduleId) {
        scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공연 회차를 찾을 수 없습니다."));

        //32. 조회해서 리스트로 넣음  
        List<Seat> seats = seatRepository.findByScheduleIdOrderByIdAsc(scheduleId);

        // 해당 회차의 선점이나 확정된 좌석 가져옴 
        List<Reservation> activeReservations = reservationRepository.findByScheduleIdAndStatusIn(
                scheduleId, List.of(ReservationStatus.HELD, ReservationStatus.CONFIRMED)
        );

       
        Map<Long, Reservation> reservationMap = activeReservations.stream()
                .collect(Collectors.toMap(
                        r -> r.getSeat().getId(),
                        r -> r,
                        (existing, replacement) -> replacement.getId() > existing.getId() ? replacement : existing
                ));

        //33. 10분 지난 좌석 있으면 만료로 만듬 
        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.HELD) {
                Reservation r = reservationMap.get(seat.getId());
                if (r != null && r.getStatus() == ReservationStatus.HELD && r.isExpired()) {
                    r.expire();
                    seat.updateStatus(SeatStatus.AVAILABLE);
                    reservationMap.remove(seat.getId());
                }
            }
        }

        //34. seatresponse로 이동
        //36.받아온 좌석을 리스트로 만들어서 반환 
        return seats.stream().map(seat -> {
            Reservation r = reservationMap.get(seat.getId());
            if (r != null) {
                if (seat.getStatus() == SeatStatus.HELD) {
                    return SeatResponse.from(seat, r.getHoldExpiresAt(), r.getId());
                } else if (seat.getStatus() == SeatStatus.RESERVED) {
                    return SeatResponse.from(seat, null, r.getId());
                }
            }
            return SeatResponse.from(seat);
        }).toList();
    }
    //46. 트랜잭션, 좌석선점 (Redis 락 최우선 획득)
    @Transactional
    public ReservationResponse holdSeat(Long memberId, ReservationHoldRequest request) {
        // 1. 최우선 Redis 10분 선점 락 획득 (메모리 단에서 99.9% 동시성 차단)
        String redisKey = "seat:hold:" + request.scheduleId() + ":" + request.seatId();
        Boolean isSet = redisTemplate.opsForValue().setIfAbsent(redisKey, memberId.toString(), Duration.ofMinutes(10));
        if (Boolean.FALSE.equals(isSet)) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 다른 사용자가 선점 중인 좌석입니다 (Redis).");
        }

        try {
            // 2. 락 획득 성공자만 DB 회차 조회
            PerformanceSchedule schedule = scheduleRepository.findById(request.scheduleId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공연 회차를 찾을 수 없습니다."));

            // 3. 티켓 오픈 시간 검사
            if (LocalDateTime.now().isBefore(schedule.getTicketOpenTime())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "티켓 오픈 시간 전입니다.");
            }

            // 4. DB 좌석 조회 및 상태 검사
            Seat seat = seatRepository.findById(request.seatId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."));

            if (seat.getStatus() == SeatStatus.RESERVED) {
                throw new BusinessException(HttpStatus.CONFLICT, "예매 완료된 좌석입니다.");
            }

            // 5. DB 상태 변경 및 예약 생성
            seat.updateStatus(SeatStatus.HELD);
            Reservation reservation = new Reservation(memberId, schedule, seat, seat.getPrice(), 10);
            return ReservationResponse.from(reservationRepository.save(reservation));
        } catch (Exception e) {
            // DB 예외 시 락 해제
            redisTemplate.delete(redisKey);
            throw e;
        }
    }
    //59. 예약 확인 
    // 타인이 들어왔을때 막기 
    @Transactional
    public ReservationResponse confirmReservation(Long memberId, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndMemberId(reservationId, memberId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "예매 내역을 찾을 수 없습니다."));
        //60. 만료됫을때 
        if (reservation.isExpired()) {
            reservation.expire();
            reservation.getSeat().updateStatus(SeatStatus.AVAILABLE);
            //redis 키 삭제
            redisTemplate.delete("seat:hold:" + reservation.getSchedule().getId() + ":" + reservation.getSeat().getId());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "선점 시간이 만료되었습니다. 다시 시도해 주세요.");
        }
        //61. 컨펌으로 상태 바꾸고, 예약됨으로 바꾸고 반환 
        reservation.confirm();
        reservation.getSeat().updateStatus(SeatStatus.RESERVED);
        //redis 키 삭제
        redisTemplate.delete("seat:hold:" + reservation.getSchedule().getId() + ":" + reservation.getSeat().getId());
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long memberId, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndMemberId(reservationId, memberId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "예매 내역을 찾을 수 없습니다."));

        if (reservation.getStatus() == ReservationStatus.HELD) {
            // 1. 선점 중 취소: 즉시 선점 해제 및 Redis 락 삭제
            reservation.cancel();
            reservation.getSeat().updateStatus(SeatStatus.AVAILABLE);
            redisTemplate.delete("seat:hold:" + reservation.getSchedule().getId() + ":" + reservation.getSeat().getId());
        } else if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            // 2. 예매 확정 취소: 환불 처리 완료 시 예매 취소 및 좌석 복구
            reservation.cancel();
            reservation.getSeat().updateStatus(SeatStatus.AVAILABLE);
        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "취소할 수 없는 예매 상태입니다.");
        }

        return ReservationResponse.from(reservation);
    }

    public List<ReservationResponse> getMyReservations(Long memberId) {
        return reservationRepository.findByMemberIdOrderByIdDesc(memberId)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
