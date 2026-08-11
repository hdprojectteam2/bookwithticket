package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.domain.reservation.Seat;
import com.example.bookwithticket.domain.reservation.SeatRepository;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//자동으로 run 
@Component
public class PerformanceDataInitializer implements CommandLineRunner {

    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;

    public PerformanceDataInitializer(
            PerformanceRepository performanceRepository,
            PerformanceScheduleRepository scheduleRepository,
            SeatRepository seatRepository) {
        this.performanceRepository = performanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    public void run(String... args) {
    //    if (performanceRepository.count() > 0) {
      //      return;
     //   }

        // 샘플 2개
        Performance p1 = new Performance(
                "뮤지컬 ",
                PerformanceCategory.MUSICAL,
                "극장",
                "https://example.com/poster1.jpg",
                170,
                "뮤지컬 설명",
                1L
        );

        Performance p2 = new Performance(
                "콘서트  ",
                PerformanceCategory.CONCERT,
                "공연장",
                "https://example.com/poster2.jpg",
                180,
                "콘서트설명",
                null
        );

        performanceRepository.save(p1);
        performanceRepository.save(p2);

        LocalDateTime now = LocalDateTime.now();

      
        PerformanceSchedule s1 = new PerformanceSchedule(p1, now.plusDays(5), now.minusHours(1));
        scheduleRepository.save(s1);
        for (int i = 1; i <= 10; i++) {
            seatRepository.save(new Seat(s1, "A-" + i, 150000));
        }

        //2회차는 열리지 않게 
        PerformanceSchedule s2 = new PerformanceSchedule(p2, now.plusDays(10), now.minusHours(2));
        PerformanceSchedule s3 = new PerformanceSchedule(p2, now.plusDays(11), now.plusHours(2));
        scheduleRepository.save(s2);
        scheduleRepository.save(s3);

        for (int i = 1; i <= 10; i++) {
            seatRepository.save(new Seat(s2, "VIP-" + i, 180000));
        }
        for (int i = 1; i <= 10; i++) {
            seatRepository.save(new Seat(s3, "VIP-" + i, 180000));
        }
    }
}
