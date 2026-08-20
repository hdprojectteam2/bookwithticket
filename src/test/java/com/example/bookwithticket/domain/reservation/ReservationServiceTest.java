package com.example.bookwithticket.domain.reservation;

import com.example.bookwithticket.domain.performance.Performance;
import com.example.bookwithticket.domain.performance.PerformanceCategory;
import com.example.bookwithticket.domain.performance.PerformanceRepository;
import com.example.bookwithticket.domain.performance.PerformanceSchedule;
import com.example.bookwithticket.domain.performance.PerformanceScheduleRepository;
import com.example.bookwithticket.global.exception.BusinessException;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.repository.MemberRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private PerformanceScheduleRepository scheduleRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Member testMember;
    private PerformanceSchedule scheduleOpen;
    private PerformanceSchedule scheduleFuture;
    private Seat seatOpen1;
    private Seat seatOpen2;
    private Seat seatFuture;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(
                Member.createLocalMember(
                        "test_user@example.com",
                        "password123",
                        "테스터",
                        "010-1111-2222",
                        "12345",
                        "서울시",
                        "101호",
                        true
                )
        );

        Performance perf = performanceRepository.save(
                new Performance("테스트 뮤지컬", PerformanceCategory.MUSICAL, "샤롯데씨어터", "poster.jpg", 120, "설명", null)
        );

        LocalDateTime now = LocalDateTime.now();
        // 이미 오픈된 회차 (현재시각 1시간 전)
        scheduleOpen = scheduleRepository.save(new PerformanceSchedule(perf, now.plusDays(1), now.minusHours(1)));
        // 오픈 전 회차 (현재시각 24시간 후 오픈 예정)
        scheduleFuture = scheduleRepository.save(new PerformanceSchedule(perf, now.plusDays(2), now.plusHours(24)));

        seatOpen1 = seatRepository.save(new Seat(scheduleOpen, "[1섹터 (1~64번)] VIP-1번", 150000));
        seatOpen2 = seatRepository.save(new Seat(scheduleOpen, "[1섹터 (1~64번)] VIP-2번", 150000));
        seatFuture = seatRepository.save(new Seat(scheduleFuture, "[1섹터 (1~64번)] VIP-1번", 150000));

        // Redis 테스트 키 초기화
        redisTemplate.delete("seat:hold:" + scheduleOpen.getId() + ":" + seatOpen1.getId());
        redisTemplate.delete("seat:hold:" + scheduleOpen.getId() + ":" + seatOpen2.getId());
        redisTemplate.delete("seat:hold:" + scheduleFuture.getId() + ":" + seatFuture.getId());
    }

    @Test
    @DisplayName("1. 정상 좌석 선점 테스트 - Redis 분산락 획득 및 10분 임시 선점")
    void holdSeat_Success() {
        ReservationHoldRequest request = new ReservationHoldRequest(scheduleOpen.getId(), seatOpen1.getId());

        ReservationResponse response = reservationService.holdSeat(testMember.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.seatNumber()).isEqualTo(seatOpen1.getSeatNumber());
        assertThat(seatOpen1.getStatus()).isEqualTo(SeatStatus.HELD);
    }

    @Test
    @DisplayName("2. 예외 검증 - 회차Id와 좌석 소속 회차 Mismatch 시 400 Bad Request 차단 예외 발생")
    void holdSeat_MismatchScheduleAndSeat_ThrowsException() {
        // scheduleOpen 회차 ID에 scheduleFuture 소속 좌석 seatFuture 조작 전달
        ReservationHoldRequest mismatchRequest = new ReservationHoldRequest(scheduleOpen.getId(), seatFuture.getId());

        assertThatThrownBy(() -> reservationService.holdSeat(testMember.getId(), mismatchRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("요청한 회차와 좌석의 소속 회차가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("3. 예외 검증 - 존재하지 않는 회차Id 요청 시 404 Not Found 예외 발생")
    void holdSeat_NonExistentSchedule_ThrowsNotFound() {
        ReservationHoldRequest invalidScheduleRequest = new ReservationHoldRequest(999999L, seatOpen1.getId());

        assertThatThrownBy(() -> reservationService.holdSeat(testMember.getId(), invalidScheduleRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                .hasMessageContaining("공연 회차를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("4. 예외 검증 - 존재하지 않는 좌석Id 요청 시 404 Not Found 예외 발생")
    void holdSeat_NonExistentSeat_ThrowsNotFound() {
        ReservationHoldRequest invalidSeatRequest = new ReservationHoldRequest(scheduleOpen.getId(), 999999L);

        assertThatThrownBy(() -> reservationService.holdSeat(testMember.getId(), invalidSeatRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                .hasMessageContaining("좌석을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("5. 예외 검증 - 티켓 오픈 전 회차 선점 시도 시 400 Bad Request 차단 예외 발생")
    void holdSeat_BeforeTicketOpenTime_ThrowsBadRequest() {
        ReservationHoldRequest futureHoldRequest = new ReservationHoldRequest(scheduleFuture.getId(), seatFuture.getId());

        assertThatThrownBy(() -> reservationService.holdSeat(testMember.getId(), futureHoldRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("티켓 오픈 시간 전입니다.");
    }

    @Test
    @DisplayName("6. 예외 검증 - 중복 선점 방지 (Redis 분산락 및 DB HELD/RESERVED 이중 방어 409 Conflict 예외 발생)")
    void holdSeat_DuplicateHold_ThrowsConflict() {
        ReservationHoldRequest request = new ReservationHoldRequest(scheduleOpen.getId(), seatOpen1.getId());
        reservationService.holdSeat(testMember.getId(), request);

        // 동일 좌석 재선점 시도
        assertThatThrownBy(() -> reservationService.holdSeat(testMember.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("7. 예외 검증 - Redis 키 유실 상황 대비 DB 레벨 HELD 이중 방어 409 Conflict 예외 발생")
    void holdSeat_DbLevelHeldStatus_ThrowsConflict() {
        // DB 좌석 상태만 HELD로 직접 설정 (Redis 키 유실 상황 가정)
        seatOpen1.updateStatus(SeatStatus.HELD);
        seatRepository.save(seatOpen1);

        ReservationHoldRequest request = new ReservationHoldRequest(scheduleOpen.getId(), seatOpen1.getId());

        assertThatThrownBy(() -> reservationService.holdSeat(testMember.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("이미 선점 중이거나 예매 완료된 좌석입니다.");
    }

    @Test
    @DisplayName("8. 예외 검증 - 10분 선점 만료 후 예매 확정 시도 시 400 Bad Request 차단 예외 발생")
    void confirmReservation_ExpiredHold_ThrowsBadRequest() {
        ReservationHoldRequest request = new ReservationHoldRequest(scheduleOpen.getId(), seatOpen1.getId());
        ReservationResponse holdRes = reservationService.holdSeat(testMember.getId(), request);

        // 선점 만료 시각을 현재 시각보다 과거로 만료 처리
        Reservation reservation = reservationRepository.findById(holdRes.id()).orElseThrow();
        reservation.expire();
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> reservationService.confirmReservation(testMember.getId(), holdRes.id()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("선점 시간이 만료되었습니다.");
    }

    @Test
    @DisplayName("9. 예외 검증 - 이미 취소된 예매에 대한 중복 취소 시도 시 400 Bad Request 예외 발생")
    void cancelReservation_InvalidStatus_ThrowsBadRequest() {
        ReservationHoldRequest request = new ReservationHoldRequest(scheduleOpen.getId(), seatOpen1.getId());
        ReservationResponse holdRes = reservationService.holdSeat(testMember.getId(), request);

        // 1차 취소 성공
        reservationService.cancelReservation(testMember.getId(), holdRes.id());

        // 2차 중복 취소 시도 ➔ 400 Bad Request
        assertThatThrownBy(() -> reservationService.cancelReservation(testMember.getId(), holdRes.id()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("취소할 수 없는 예매 상태입니다.");
    }

    @Test
    @DisplayName("10. 예매 생명주기 전체 통합 검증 - 선점 -> 확정 -> 예매 취소/환불 (CANCELLED 상태 교정 확인)")
    void confirmAndCancelReservation_Success() {
        ReservationHoldRequest request = new ReservationHoldRequest(scheduleOpen.getId(), seatOpen1.getId());
        ReservationResponse holdRes = reservationService.holdSeat(testMember.getId(), request);

        // 예매 확정 (HELD -> CONFIRMED)
        ReservationResponse confirmRes = reservationService.confirmReservation(testMember.getId(), holdRes.id());
        assertThat(confirmRes.status()).isEqualTo("CONFIRMED");
        assertThat(seatOpen1.getStatus()).isEqualTo(SeatStatus.RESERVED);

        // 예매 취소 (CONFIRMED -> CANCELLED, 좌석 AVAILABLE 복구)
        ReservationResponse cancelRes = reservationService.cancelReservation(testMember.getId(), holdRes.id());
        assertThat(cancelRes.status()).isEqualTo("CANCELLED");
        assertThat(seatOpen1.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }
}
