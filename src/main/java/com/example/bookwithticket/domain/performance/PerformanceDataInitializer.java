package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.repository.BookRepository;
import com.example.bookwithticket.cart.repository.CartItemRepository;
import com.example.bookwithticket.cart.repository.PerformanceCartItemRepository;
import com.example.bookwithticket.domain.reservation.ReservationRepository;
import com.example.bookwithticket.domain.reservation.Seat;
import com.example.bookwithticket.domain.reservation.SeatRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PerformanceDataInitializer implements CommandLineRunner {

    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final PerformanceCartItemRepository performanceCartItemRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;

    public PerformanceDataInitializer(
            PerformanceRepository performanceRepository,
            PerformanceScheduleRepository scheduleRepository,
            SeatRepository seatRepository,
            ReservationRepository reservationRepository,
            PerformanceCartItemRepository performanceCartItemRepository,
            CartItemRepository cartItemRepository,
            BookRepository bookRepository) {
        this.performanceRepository = performanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.performanceCartItemRepository = performanceCartItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 동시성 테스트용 더미 공연이 DB에 있다면 비활성화(deactivate)하여 일반 사용자에게 숨김 처리 (관리자에게만 노출)
        performanceRepository.findByTitleContainingOrderByIdDesc("동시성 검증 뮤지컬")
                .forEach(p -> {
                    if (p.isActive()) {
                        p.deactivate();
                        performanceRepository.save(p);
                    }
                });

        // 3. 테스트용 샘플 도서 생성 (원작 도서: 오페라의 유령)
        String sampleIsbn = "9788932909000";
        Book sampleBook = bookRepository.findByIsbn(sampleIsbn)
                .orElseGet(() -> {
                    Book b = Book.create(
                            sampleIsbn,
                            "오페라의 유령 (원작 소설)",
                            "가스통 르루",
                            "열린책들",
                            15000,
                            13500,
                            "https://image.aladin.co.kr/product/57/88/cover500/8932909000_1.jpg",
                            "19세기 파리 오페라 극장의 지하에 숨어 사는 신비로운 존재 팬텀과 아름다운 프리마돈나 크리스틴의 매혹적이고 비극적인 사랑을 그린 세계적인 고전 명작 소설.",
                            "소설",
                            "국내도서>소설/시/희곡>서양고전문학",
                            LocalDate.of(2023, 1, 15),
                            100
                    );
                    return bookRepository.save(b);
                });

        // 4. 테스트용 연동 샘플 공연 및 회차/좌석 생성 (원작 도서와 양방향 연동)
        boolean perfExists = !performanceRepository.findByTitleContainingOrderByIdDesc("오페라의 유령").isEmpty();
        if (!perfExists) {
            Performance samplePerf = new Performance(
                    "뮤지컬 오페라의 유령 (The Phantom of the Opera)",
                    PerformanceCategory.MUSICAL,
                    "샤롯데씨어터",
                    "https://image.aladin.co.kr/product/57/88/cover500/8932909000_1.jpg",
                    150,
                    "세계 4대 뮤지컬의 거장 앤드루 로이드 웨버의 대표작. 원작 소설의 낭만과 웅장한 오케스트라 사운드가 결합된 불멸의 뮤지컬 걸작.",
                    sampleBook.getId()
            );
            samplePerf.setSeatscale(128);
            Performance savedPerf = performanceRepository.save(samplePerf);

            // 회차 1 (내일 19:30, 티켓 오픈시간: 1시간 전 -> 현재 즉시 예매 가능)
            PerformanceSchedule sch1 = new PerformanceSchedule(
                    savedPerf,
                    LocalDateTime.now().plusDays(1).withHour(19).withMinute(30).withSecond(0),
                    LocalDateTime.now().minusHours(1)
            );
            scheduleRepository.save(sch1);

            // 회차 2 (모레 14:00, 티켓 오픈시간: 1시간 전 -> 현재 즉시 예매 가능)
            PerformanceSchedule sch2 = new PerformanceSchedule(
                    savedPerf,
                    LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0),
                    LocalDateTime.now().minusHours(1)
            );
            scheduleRepository.save(sch2);

            // 회차 1에 128석(2개 섹터) 배치 자동 생성
            List<Seat> seats = new ArrayList<>();
            for (int i = 1; i <= 128; i++) {
                int sectorNum = ((i - 1) / 64) + 1;
                int startSeat = (sectorNum - 1) * 64 + 1;
                int endSeat = sectorNum * 64;
                String tier = (i <= 16) ? "VIP" : (i <= 32) ? "R" : (i <= 80) ? "S" : "A";
                int price = (i <= 16) ? 180000 : (i <= 32) ? 150000 : (i <= 80) ? 100000 : 70000;
                String seatNumber = String.format("[%d섹터 (%d~%d번)] %s-%d번", sectorNum, startSeat, endSeat, tier, i);
                seats.add(new Seat(sch1, seatNumber, price));
            }
            seatRepository.saveAll(seats);

            System.out.println("[초기 데이터 생성] 원작 도서(ID: " + sampleBook.getId() + ") 및 연동 공연(ID: " + savedPerf.getId() + ") 128석 생성 완료");
        }
    }
}
