package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.domain.reservation.Seat;
import com.example.bookwithticket.domain.reservation.SeatRepository;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.jwt.JwtUtil;
import com.example.bookwithticket.member.repository.MemberRepository;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PerformanceDataInitializer implements CommandLineRunner {

    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    public PerformanceDataInitializer(
            PerformanceRepository performanceRepository,
            PerformanceScheduleRepository scheduleRepository,
            SeatRepository seatRepository,
            MemberRepository memberRepository,
            JwtUtil jwtUtil) {
        this.performanceRepository = performanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.seatRepository = seatRepository;
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void run(String... args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 1. 일반 사용자 계정 생성 (USER 권한)
        String testEmail = "test@example.com";
        Member userMember = memberRepository.findByEmail(testEmail)
                .orElseGet(() -> {
                    Member m = Member.createLocalMember(
                            testEmail,
                            encoder.encode("password123"),
                            "일반사용자",
                            "010-1234-5678",
                            "12345",
                            "서울시 강남구",
                            "101호",
                            true
                    );
                    m.setRole("USER");
                    return memberRepository.save(m);
                });

        // 2. 관리자 계정 생성 (ADMIN 권한)
        String adminEmail = "admin@example.com";
        Member adminMember = memberRepository.findByEmail(adminEmail)
                .orElseGet(() -> {
                    Member m = Member.createLocalMember(
                            adminEmail,
                            encoder.encode("admin123"),
                            "관리자",
                            "010-9876-5432",
                            "54321",
                            "서울시 종로구",
                            "909호",
                            true
                    );
                    m.setRole("ADMIN");
                    return memberRepository.save(m);
                });

        String userToken = jwtUtil.createToken(userMember.getEmail(), userMember.getRole());
        String adminToken = jwtUtil.createToken(adminMember.getEmail(), adminMember.getRole());

        System.out.println("==================================================");
        System.out.println("[테스트용 회원 계정 및 JWT 토큰 생성 완료]");
        System.out.println("일반 사용자 (USER): " + userMember.getEmail() + " | Token: " + userToken);
        System.out.println("관리자 계정 (ADMIN): " + adminMember.getEmail() + " | Token: " + adminToken);
        System.out.println("==================================================");

        if (performanceRepository.count() > 0) {
            return;
        }

        // 샘플 공연 데이터 생성
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
