package com.example.bookwithticket.domain.reservation;

import com.example.bookwithticket.book.dto.BookResponseDto;
import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.repository.BookRepository;
import com.example.bookwithticket.book.service.BookService;
import com.example.bookwithticket.domain.performance.Performance;
import com.example.bookwithticket.domain.performance.PerformanceCategory;
import com.example.bookwithticket.domain.performance.PerformanceRepository;
import com.example.bookwithticket.domain.performance.PerformanceSchedule;
import com.example.bookwithticket.domain.performance.PerformanceScheduleRepository;
import com.example.bookwithticket.global.exception.BusinessException;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.repository.MemberRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
class BookAndPerformanceFullFlowIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private PerformanceScheduleRepository scheduleRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Member userA;
    private Member userB;
    private Book book;
    private Performance performance;
    private PerformanceSchedule schedule;
    private Seat seat1;
    private Seat seat2;

    @BeforeEach
    void setUp() {
        // 1. 테스트 사용자 2명 생성
        userA = memberRepository.save(
                Member.createLocalMember("userA_flow@test.com", "pass123", "사용자A", "010-1111-1111", "12345", "서울", "101호", true)
        );
        userB = memberRepository.save(
                Member.createLocalMember("userB_flow@test.com", "pass123", "사용자B", "010-2222-2222", "12345", "서울", "102호", true)
        );

        // 2. 도서 데이터 생성
        book = bookRepository.save(
                Book.create(
                        "9788932909999",
                        "오페라의 유령 (E2E)",
                        "가스통 르루",
                        "열린책들",
                        15000,
                        13500,
                        "https://image.aladin.co.kr/cover.jpg",
                        "고전 명작 소설",
                        "소설",
                        "국내도서>소설",
                        LocalDate.of(2023, 1, 15),
                        50
                )
        );

        // 3. 원작 도서와 연계된 공연 데이터 생성
        performance = performanceRepository.save(
                new Performance(
                        "뮤지컬 오페라의 유령 (E2E)",
                        PerformanceCategory.MUSICAL,
                        "샤롯데씨어터",
                        "https://image.aladin.co.kr/cover.jpg",
                        150,
                        "오페라의 유령 뮤지컬 공연",
                        book.getId()
                )
        );

        // 4. 공연 회차 생성 (티켓 오픈: 1시간 전 -> 즉시 예매 가능)
        schedule = scheduleRepository.save(
                new PerformanceSchedule(
                        performance,
                        LocalDateTime.now().plusDays(3).withHour(19).withMinute(30),
                        LocalDateTime.now().minusHours(1)
                )
        );

        // 5. 회차 좌석 생성 (A-1, A-2)
        seat1 = seatRepository.save(new Seat(schedule, "[1섹터] VIP-1번", 150000));
        seat2 = seatRepository.save(new Seat(schedule, "[1섹터] VIP-2번", 150000));

        // Redis 잔여 키 정리
        redisTemplate.delete("seat:hold:" + schedule.getId() + ":" + seat1.getId());
        redisTemplate.delete("seat:hold:" + schedule.getId() + ":" + seat2.getId());
    }

    @Test
    @DisplayName("[E2E 흐름 1] 도서 조회 -> 연계 원작 공연 탐색 -> 좌석 선점 -> 결제 확정 전체 파이프라인 검증")
    void fullFlow_bookToReservationSuccess() {
        // [Step 1] 도서 상세 조회 및 연계 공연 ID 매핑 확인
        BookResponseDto bookDetail = bookService.findDetail(book.getId(), userA);
        assertThat(bookDetail).isNotNull();
        assertThat(bookDetail.getTitle()).isEqualTo("오페라의 유령 (E2E)");

        List<Performance> linkedPerformances = performanceRepository.findByTitleContainingOrderByIdDesc("오페라의 유령 (E2E)");
        assertThat(linkedPerformances).isNotEmpty();
        assertThat(linkedPerformances.get(0).getOriginalBookId()).isEqualTo(book.getId());

        // [Step 2] 회차의 좌석 조회 (AVAILABLE 상태 확인)
        Seat freshSeat = seatRepository.findById(seat1.getId()).orElseThrow();
        assertThat(freshSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);

        // [Step 3] 사용자 A가 좌석 선점 (Hold) 호출 -> Redis 10분 TTL 락 획득
        ReservationHoldRequest holdRequest = new ReservationHoldRequest(schedule.getId(), seat1.getId());
        ReservationResponse holdResponse = reservationService.holdSeat(userA.getId(), holdRequest);

        assertThat(holdResponse).isNotNull();
        assertThat(holdResponse.status()).isEqualTo("HELD");
        assertThat(holdResponse.seatNumber()).isEqualTo(seat1.getSeatNumber());

        // DB 좌석 상태 및 예약 임시 상태 검증
        Seat heldSeat = seatRepository.findById(seat1.getId()).orElseThrow();
        assertThat(heldSeat.getStatus()).isEqualTo(SeatStatus.HELD);

        Reservation pendingReservation = reservationRepository.findById(holdResponse.id()).orElseThrow();
        assertThat(pendingReservation.getStatus()).isEqualTo(ReservationStatus.HELD);
        assertThat(pendingReservation.getMemberId()).isEqualTo(userA.getId());

        // [Step 4] 사용자 A가 결제 완료 (예매 확정) 호출
        ReservationResponse confirmResponse = reservationService.confirmReservation(userA.getId(), pendingReservation.getId());
        assertThat(confirmResponse).isNotNull();
        assertThat(confirmResponse.status()).isEqualTo("CONFIRMED");

        // 좌석 상태가 최종 RESERVED 로 변경되었는지 검증
        Seat reservedSeat = seatRepository.findById(seat1.getId()).orElseThrow();
        assertThat(reservedSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
    }

    @Test
    @DisplayName("[E2E 흐름 2] 사용자 A가 좌석 선점 중일 때, 사용자 B의 동시 선점 시도 차단 검증 (409 Conflict)")
    void fullFlow_concurrentHoldBlocked() {
        // [Step 1] 사용자 A가 먼저 좌석 1 선점
        ReservationHoldRequest holdRequest1 = new ReservationHoldRequest(schedule.getId(), seat1.getId());
        reservationService.holdSeat(userA.getId(), holdRequest1);

        // [Step 2] 사용자 B가 동일한 좌석 1 선점 시도 -> 비즈니스 예외(409 CONFLICT) 발생
        ReservationHoldRequest duplicateRequest = new ReservationHoldRequest(schedule.getId(), seat1.getId());
        assertThatThrownBy(() -> reservationService.holdSeat(userB.getId(), duplicateRequest))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                });

        // [Step 3] 사용자 B는 비어있는 다른 좌석(A-2)은 정상 선점 가능해야 함
        ReservationHoldRequest holdRequest2 = new ReservationHoldRequest(schedule.getId(), seat2.getId());
        ReservationResponse holdB = reservationService.holdSeat(userB.getId(), holdRequest2);
        assertThat(holdB.status()).isEqualTo("HELD");
        assertThat(holdB.seatNumber()).isEqualTo(seat2.getSeatNumber());
    }

    @Test
    @DisplayName("[E2E 흐름 3] 예매 취소 및 환불 시 좌석이 다시 AVAILABLE 로 원상 복구되는지 검증")
    void fullFlow_reservationCancelAndSeatRestoration() {
        // [Step 1] 선점 및 예매 확정 완료
        ReservationHoldRequest holdRequest = new ReservationHoldRequest(schedule.getId(), seat1.getId());
        ReservationResponse holdResponse = reservationService.holdSeat(userA.getId(), holdRequest);
        ReservationResponse confirmResponse = reservationService.confirmReservation(userA.getId(), holdResponse.id());
        assertThat(confirmResponse.status()).isEqualTo("CONFIRMED");

        // [Step 2] 예매 취소/환불 실행
        reservationService.cancelReservation(userA.getId(), confirmResponse.id());

        // [Step 3] 예약 상태는 CANCELLED, 좌석 상태는 다시 AVAILABLE 로 복구 검증
        Reservation cancelledReservation = reservationRepository.findById(confirmResponse.id()).orElseThrow();
        assertThat(cancelledReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);

        Seat restoredSeat = seatRepository.findById(seat1.getId()).orElseThrow();
        assertThat(restoredSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);

        // [Step 4] 취소된 좌석을 사용자 B가 즉시 재선점 가능한지 검증
        ReservationHoldRequest newHoldRequest = new ReservationHoldRequest(schedule.getId(), seat1.getId());
        ReservationResponse newHold = reservationService.holdSeat(userB.getId(), newHoldRequest);
        assertThat(newHold.status()).isEqualTo("HELD");
    }
}
