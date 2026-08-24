package com.example.bookwithticket.domain.reservation;

import com.example.bookwithticket.domain.performance.Performance;
import com.example.bookwithticket.domain.performance.PerformanceCategory;
import com.example.bookwithticket.domain.performance.PerformanceRepository;
import com.example.bookwithticket.domain.performance.PerformanceSchedule;
import com.example.bookwithticket.domain.performance.PerformanceScheduleRepository;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReservationConcurrencyTest {

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
    private StringRedisTemplate redisTemplate;

    private List<Member> testMembers = new ArrayList<>();
    private PerformanceSchedule testSchedule;
    private Seat targetSeat;

    @BeforeEach
    void setUp() {
        // 50명의 동시 요청 사용자 생성
        testMembers.clear();
        for (int i = 1; i <= 50; i++) {
            final int index = i;
            String email = "concurrent_user_" + index + "@example.com";
            String name = "동시테스터" + index;
            String phone = "010-0000-" + String.format("%04d", index);
            Member member = memberRepository.findByEmail(email)
                    .orElseGet(() -> memberRepository.save(
                            Member.createLocalMember(
                                    email, "pass1234", name, phone,
                                    "12345", "서울시", "101호", true
                            )
                    ));
            testMembers.add(member);
        }

        Performance perf = new Performance("동시성 검증 뮤지컬", PerformanceCategory.MUSICAL, "블루스퀘어", "poster.jpg", 150, "동시성 테스트용 공연", null);
        perf.deactivate(); // 비활성화 처리하여 일반 사용자 목록에 노출 방지 (관리자에게만 노출)
        perf = performanceRepository.save(perf);

        testSchedule = scheduleRepository.save(
                new PerformanceSchedule(perf, LocalDateTime.now().plusDays(1), LocalDateTime.now().minusHours(1))
        );

        targetSeat = seatRepository.save(
                new Seat(testSchedule, "[1섹터 (1~64번)] VIP-99번", 180000)
        );

        // Redis 잔여 키 정리
        redisTemplate.delete("seat:hold:" + testSchedule.getId() + ":" + targetSeat.getId());
    }

    @Test
    @DisplayName("⚡ [동시성 테스트] 50명의 사용자가 동일한 단 1개 좌석을 동시에 선점 시도 시 -> 정확히 1명만 성공하고 49명은 실패(409 Conflict)해야 한다")
    void holdSeat_Concurrent50Requests_OnlyOneSucceeds() throws InterruptedException {
        int numberOfThreads = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ReservationHoldRequest holdRequest = new ReservationHoldRequest(testSchedule.getId(), targetSeat.getId());

        for (int i = 0; i < numberOfThreads; i++) {
            final Member currentMember = testMembers.get(i);
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 동시에 출발하도록 대기
                    reservationService.holdSeat(currentMember.getId(), holdRequest);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 50개 스레드 동시 발사!
        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        // 검증: 오직 1명만 성공, 49명은 실패
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(49);

        // DB 좌석 상태가 HELD 인지 확인
        Seat updatedSeat = seatRepository.findById(targetSeat.getId()).orElseThrow();
        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.HELD);
    }
}
