package com.example.bookwithticket.domain.reservation;

import com.example.bookwithticket.global.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }
    //29. 도착 
    @GetMapping("/performances/schedules/{scheduleId}/seats")
    public ApiResponse<List<SeatResponse>> getSeats(@PathVariable("scheduleId") Long scheduleId) {
    	//30. reservationservice로 이동
    	//37. 받아온 좌석을 반환
        return ApiResponse.ok(reservationService.getSeats(scheduleId));
    }
    //44. 선점 요청을 받음 
    @PostMapping("/reservations/hold")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> holdSeat(
            @RequestHeader(value = "X-Member-Id", defaultValue = "1") Long memberId,
            @Valid @RequestBody ReservationHoldRequest request) {
    	//45.reservationservice로 이동 
    	//51. 선점완료
        return ApiResponse.ok("좌석이 10분간 선점되었습니다.", reservationService.holdSeat(memberId, request));
    }
//58. reservationservice로 이동
    @PostMapping("/reservations/{reservationId}/confirm")
    public ApiResponse<ReservationResponse> confirmReservation(
            @RequestHeader(value = "X-Member-Id", defaultValue = "1") Long memberId,
            @PathVariable("reservationId") Long reservationId) {
    	//62. 예매 상태 변경후 반환 
        return ApiResponse.ok("예매가 확정되었습니다.", reservationService.confirmReservation(memberId, reservationId));
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    public ApiResponse<ReservationResponse> cancelReservation(
            @RequestHeader(value = "X-Member-Id", defaultValue = "1") Long memberId,
            @PathVariable("reservationId") Long reservationId) {
        return ApiResponse.ok("예매가 취소되었습니다.", reservationService.cancelReservation(memberId, reservationId));
    }
}
