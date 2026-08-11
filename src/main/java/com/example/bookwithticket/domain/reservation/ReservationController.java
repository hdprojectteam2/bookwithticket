package com.example.bookwithticket.domain.reservation;

import com.example.bookwithticket.global.common.ApiResponse;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.repository.MemberRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;
    private final MemberRepository memberRepository;

    public ReservationController(
            ReservationService reservationService,
            MemberRepository memberRepository
    ) {
        this.reservationService = reservationService;
        this.memberRepository = memberRepository;
    }

    @GetMapping("/performances/schedules/{scheduleId}/seats")
    public ApiResponse<List<SeatResponse>> getSeats(@PathVariable("scheduleId") Long scheduleId) {
        return ApiResponse.ok(reservationService.getSeats(scheduleId));
    }

    @PostMapping("/reservations/hold")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> holdSeat(
            Authentication authentication,
            @Valid @RequestBody ReservationHoldRequest request) {
        Member member = getMember(authentication);
        return ApiResponse.ok("좌석이 10분간 선점되었습니다.", reservationService.holdSeat(member.getId(), request));
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ApiResponse<ReservationResponse> confirmReservation(
            Authentication authentication,
            @PathVariable("reservationId") Long reservationId) {
        Member member = getMember(authentication);
        return ApiResponse.ok("예매가 확정되었습니다.", reservationService.confirmReservation(member.getId(), reservationId));
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    public ApiResponse<ReservationResponse> cancelReservation(
            Authentication authentication,
            @PathVariable("reservationId") Long reservationId) {
        Member member = getMember(authentication);
        return ApiResponse.ok("예매가 취소되었습니다.", reservationService.cancelReservation(member.getId(), reservationId));
    }

    private Member getMember(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }
}
