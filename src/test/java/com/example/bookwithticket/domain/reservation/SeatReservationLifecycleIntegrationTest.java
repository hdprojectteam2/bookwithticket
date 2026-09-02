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
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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
class SeatReservationLifecycleIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private PerformanceScheduleRepository scheduleRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Member userA;
    private Member userB;
    private PerformanceSchedule schedule;
    private Seat seatVip;
    private Seat seatR;

    @BeforeEach
    void setUp() {
        // 1. 테스트 사용자 2명 생성
        userA = memberRepository.save(
                Member.createLocalMember("lifecycle_userA@test.com", "pass123", "사용자A", "010-1111-1111", "12345", "서울", "101호", true)
        );
        userB = memberRepository.save(
                Member.createLocalMember("lifecycle_userB@test.com", "pass123", "사용자B", "010-2222-2222", "12345", "서울", "102호", true)
        );

        // 2. 공연 및 회차 생성
        Performance performance = performanceRepository.save(
                new Performance("라이프사이클 검증 공연", PerformanceCategory.MUSICAL, "샤롯데씨어터", "https://example.com/poster.jpg", 150, "설명", null)
        );

        schedule = scheduleRepository.save(
                new PerformanceSchedule(performance, LocalDateTime.now().plusDays(5), LocalDateTime.now().minusDays(1))
        );

        // 3. VIP석 및 R석 생성
        seatVip = seatRepository.save(
                new Seat(schedule, "[1섹터 (1~64번)] VIP-1번", 180000)
        );
        seatR = seatRepository.save(
                new Seat(schedule, "[1섹터 (1~64번)] R-17번", 150000)
        );
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 Redis 키 정리
        redisTemplate.delete("seat:hold:" + schedule.getId() + ":" + seatVip.getId());
        redisTemplate.delete("seat:hold:" + schedule.getId() + ":" + seatR.getId());
    }

    @Test
    @DisplayName("1. 좌석 선점 ➔ 선점 취소 ➔ Redis 락 삭제 및 좌석 복구(AVAILABLE) ➔ 타인 재선점 성공 검증")
    void holdSeat_and_cancelHold_and_rehold_success() {
        // [Step 1] 사용자 A가 VIP 좌석 선점
        ReservationHoldRequest request = new ReservationHoldRequest(schedule.getId(), seatVip.getId());
        ReservationResponse holdResponse = reservationService.holdSeat(userA.getId(), request);

        assertThat(holdResponse.status()).isEqualTo("HELD");
        assertThat(seatVip.getStatus()).isEqualTo(SeatStatus.HELD);
        String redisKey = "seat:hold:" + schedule.getId() + ":" + seatVip.getId();
        assertThat(redisTemplate.hasKey(redisKey)).isTrue();
        assertThat(redisTemplate.opsForValue().get(redisKey)).isEqualTo(userA.getId().toString());

        // [Step 2] 선점 중 다른 사용자 B가 선점 시도 시 차단(409 CONFLICT)
        assertThatThrownBy(() -> reservationService.holdSeat(userB.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);

        // [Step 3] 사용자 A가 선점 취소 실행
        ReservationResponse cancelResponse = reservationService.cancelReservation(userA.getId(), holdResponse.id());
        assertThat(cancelResponse.status()).isEqualTo("CANCELLED");
        assertThat(seatVip.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(redisTemplate.hasKey(redisKey)).isFalse(); // Redis 락 즉시 소멸 확인

        // [Step 4] 사용자 B가 즉시 동일 좌석 재선점 성공
        ReservationResponse userBHoldResponse = reservationService.holdSeat(userB.getId(), request);
        assertThat(userBHoldResponse.status()).isEqualTo("HELD");
        assertThat(redisTemplate.opsForValue().get(redisKey)).isEqualTo(userB.getId().toString());
    }

    @Test
    @DisplayName("2. 권한 방어 - 타인의 선점 건을 다른 사용자가 임의로 취소하려 할 때 404 NOT_FOUND 거절")
    void cancelReservation_by_other_user_fails() {
        // 사용자 A가 좌석 선점
        ReservationHoldRequest request = new ReservationHoldRequest(schedule.getId(), seatVip.getId());
        ReservationResponse holdResponse = reservationService.holdSeat(userA.getId(), request);

        // 사용자 B가 사용자 A의 예매 ID로 취소 시도 ➔ 차단
        assertThatThrownBy(() -> reservationService.cancelReservation(userB.getId(), holdResponse.id()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);

        // 여전히 사용자 A의 선점 상태와 Redis 락 유지
        assertThat(seatVip.getStatus()).isEqualTo(SeatStatus.HELD);
        String redisKey = "seat:hold:" + schedule.getId() + ":" + seatVip.getId();
        assertThat(redisTemplate.hasKey(redisKey)).isTrue();
    }

    @Test
    @DisplayName("3. 선점 ➔ 결제 확정(CONFIRM) ➔ 환불 취소 ➔ 좌석 복구 및 재예매 사이클 검증")
    void hold_confirm_and_refund_flow_success() {
        // [Step 1] 사용자 A가 좌석 선점
        ReservationHoldRequest request = new ReservationHoldRequest(schedule.getId(), seatVip.getId());
        ReservationResponse holdResponse = reservationService.holdSeat(userA.getId(), request);

        // [Step 2] 사용자 A가 결제 확정(CONFIRM)
        ReservationResponse confirmResponse = reservationService.confirmReservation(userA.getId(), holdResponse.id());
        assertThat(confirmResponse.status()).isEqualTo("CONFIRMED");
        assertThat(seatVip.getStatus()).isEqualTo(SeatStatus.RESERVED);
        String redisKey = "seat:hold:" + schedule.getId() + ":" + seatVip.getId();
        assertThat(redisTemplate.hasKey(redisKey)).isFalse(); // 결제 확정 시 Redis 임시 선점 락 해제

        // [Step 3] 결제 완료 후 사용자 A가 예매 취소(환불) 실행
        ReservationResponse refundResponse = reservationService.cancelReservation(userA.getId(), confirmResponse.id());
        assertThat(refundResponse.status()).isEqualTo("CANCELLED");
        assertThat(seatVip.getStatus()).isEqualTo(SeatStatus.AVAILABLE); // 좌석 다시 예매 가능 복구

        // [Step 4] 다른 사용자 B가 취소된 좌석 정상 예매 가능 확인
        ReservationResponse newHold = reservationService.holdSeat(userB.getId(), request);
        assertThat(newHold.status()).isEqualTo("HELD");
    }

    @Test
    @DisplayName("4. getSeats 조회 시 선점 좌석의 reservationId 및 만료시각 정확 반환 검증")
    void getSeats_returns_active_reservation_details() {
        // 사용자 A가 VIP석 선점
        ReservationHoldRequest request = new ReservationHoldRequest(schedule.getId(), seatVip.getId());
        ReservationResponse holdResponse = reservationService.holdSeat(userA.getId(), request);

        // 회차 좌석 목록 조회
        List<SeatResponse> seats = reservationService.getSeats(schedule.getId());

        SeatResponse vipSeatResp = seats.stream()
                .filter(s -> s.id().equals(seatVip.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(vipSeatResp.status()).isEqualTo("HELD");
        assertThat(vipSeatResp.reservationId()).isEqualTo(holdResponse.id());
        assertThat(vipSeatResp.holdExpiresAt()).isNotNull();

        // 미선점된 R석은 AVAILABLE 및 reservationId null
        SeatResponse rSeatResp = seats.stream()
                .filter(s -> s.id().equals(seatR.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(rSeatResp.status()).isEqualTo("AVAILABLE");
        assertThat(rSeatResp.reservationId()).isNull();
    }

    @Test
    @DisplayName("5. 만료 방어 - 10분 선점 시간 경과 후 결제 시도 시 400 차단 및 좌석 자동 복구(AVAILABLE) 검증")
    void confirm_after_expired_fails_and_releases_seat() {
        // [Step 1] 과거 시간으로 선점 만료된 예약 직접 생성
        seatVip.updateStatus(SeatStatus.HELD);
        Reservation expiredReservation = new Reservation(userA.getId(), schedule, seatVip, seatVip.getPrice(), -1); // 만료시간 1분 전
        Reservation saved = reservationRepository.save(expiredReservation);

        // [Step 2] 만료된 예매를 확정(confirm) 시도 시 거절(400)
        assertThatThrownBy(() -> reservationService.confirmReservation(userA.getId(), saved.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);

        // [Step 3] 좌석 상태가 AVAILABLE로 자동 롤백되었는지 검증
        assertThat(seatVip.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("6. 변조 방어 - 요청 회차와 좌석의 소속 회차가 불일치할 경우 400 BAD_REQUEST 거절")
    void holdSeat_schedule_seat_mismatch_fails() {
        // 다른 더미 회차 생성
        PerformanceSchedule otherSchedule = scheduleRepository.save(
                new PerformanceSchedule(schedule.getPerformance(), LocalDateTime.now().plusDays(10), LocalDateTime.now().minusDays(1))
        );

        // otherSchedule 회차로 seatVip(기존 schedule 소속)을 선점 시도
        ReservationHoldRequest mismatchRequest = new ReservationHoldRequest(otherSchedule.getId(), seatVip.getId());

        assertThatThrownBy(() -> reservationService.holdSeat(userA.getId(), mismatchRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("7. 환불 방어 - 이미 공연 시작 시간이 지난 회차는 취소/환불 시도 시 400 거절")
    void cancel_after_performance_start_fails() {
        // 이미 시작된 과거 공연 회차 생성
        PerformanceSchedule pastSchedule = scheduleRepository.save(
                new PerformanceSchedule(schedule.getPerformance(), LocalDateTime.now().minusHours(2), LocalDateTime.now().minusDays(3))
        );
        Seat pastSeat = seatRepository.save(new Seat(pastSchedule, "[1섹터] VIP-99번", 180000));
        pastSeat.updateStatus(SeatStatus.RESERVED);

        Reservation pastReservation = reservationRepository.save(
                new Reservation(userA.getId(), pastSchedule, pastSeat, pastSeat.getPrice(), 10)
        );
        pastReservation.confirm();

        // 공연 시작 후 취소 시도 ➔ 차단
        assertThatThrownBy(() -> reservationService.cancelReservation(userA.getId(), pastReservation.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("8. 내 예매 조회 - 사용자의 활성 예매 목록만 정확히 반환되는지 검증")
    void getMyReservations_accuracy() {
        // 사용자 A 좌석 선점
        ReservationHoldRequest request = new ReservationHoldRequest(schedule.getId(), seatVip.getId());
        reservationService.holdSeat(userA.getId(), request);

        // 사용자 A의 예매 목록 조회
        List<ReservationResponse> myReservations = reservationService.getMyReservations(userA.getId());
        assertThat(myReservations).hasSize(1);
        assertThat(myReservations.get(0).seatNumber()).isEqualTo(seatVip.getSeatNumber());

        // 다른 사용자 B의 예매 목록은 비어있어야 함
        List<ReservationResponse> otherReservations = reservationService.getMyReservations(userB.getId());
        assertThat(otherReservations).isEmpty();
    }
}
