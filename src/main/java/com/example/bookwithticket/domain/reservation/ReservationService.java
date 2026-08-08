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
            if (seat.getStatus() == SeatStatus.HELD && r != null) {
                return SeatResponse.from(seat, r.getHoldExpiresAt(), r.getId());
            } else if (seat.getStatus() == SeatStatus.RESERVED && r != null) {
                return SeatResponse.from(seat, null, r.getId());
            }
            return SeatResponse.from(seat);
        }).toList();
    }
    //46. 트랜잭션, 좌석선점 
    @Transactional
    public ReservationResponse holdSeat(Long memberId, ReservationHoldRequest request) {
        PerformanceSchedule schedule = scheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공연 회차를 찾을 수 없습니다."));
        	//47. performanceschedule에서 opentime가져옴
        if (LocalDateTime.now().isBefore(schedule.getTicketOpenTime())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "티켓 오픈 시간 전입니다.");
        }
        //48. reservationholdingrequest에서 좌석 존재하는지 
        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."));
        	
      
        	
        if (seat.getStatus() == SeatStatus.RESERVED){
            throw new BusinessException(HttpStatus.CONFLICT, " 예매 완료된 좌석입니다.");
        }
        //49. hold중이면 만료됫는지 확인하고 맞으면 다시 접근가능하게 
        //redis 10분 선점 락, 키가 없을떄만 , 원자성으로 한명만 선점가능 
        String redisKey = "seat:hold:" + schedule.getId() + ":" + seat.getId();
        Boolean isSet = redisTemplate.opsForValue().setIfAbsent(redisKey, memberId.toString(), Duration.ofMinutes(10));
        if (Boolean.FALSE.equals(isSet)) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 다른 사용자가 선점 중인 좌석입니다 (Redis).");
        }

        //50. hold로 변경하고 새 예약으로 만듬 
        seat.updateStatus(SeatStatus.HELD);
        Reservation reservation = new Reservation(memberId, schedule, seat, seat.getPrice(), 10);
        return ReservationResponse.from(reservationRepository.save(reservation));
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

        reservation.cancel();
        reservation.getSeat().updateStatus(SeatStatus.AVAILABLE);
        //redis 키 삭제
        redisTemplate.delete("seat:hold:" + reservation.getSchedule().getId() + ":" + reservation.getSeat().getId());
        return ReservationResponse.from(reservation);
    }
}
