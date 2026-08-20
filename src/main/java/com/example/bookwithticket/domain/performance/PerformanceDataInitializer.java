package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.cart.repository.CartItemRepository;
import com.example.bookwithticket.cart.repository.PerformanceCartItemRepository;
import com.example.bookwithticket.domain.reservation.ReservationRepository;
import com.example.bookwithticket.domain.reservation.SeatRepository;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.jwt.JwtUtil;
import com.example.bookwithticket.member.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    public PerformanceDataInitializer(
            PerformanceRepository performanceRepository,
            PerformanceScheduleRepository scheduleRepository,
            SeatRepository seatRepository,
            ReservationRepository reservationRepository,
            PerformanceCartItemRepository performanceCartItemRepository,
            CartItemRepository cartItemRepository,
            MemberRepository memberRepository,
            JwtUtil jwtUtil) {
        this.performanceRepository = performanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.performanceCartItemRepository = performanceCartItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
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

        // 3. 기존 누적 DB 데이터 초기화 (장바구니 외래 키 삭제 순서 보장)
        try {
            performanceCartItemRepository.deleteAllInBatch();
            cartItemRepository.deleteAllInBatch();
            reservationRepository.deleteAllInBatch();
            seatRepository.deleteAllInBatch();
            scheduleRepository.deleteAllInBatch();
            performanceRepository.deleteAllInBatch();
        } catch (Exception e) {
            System.err.println("[DB 초기화 경고] " + e.getMessage());
        }

        // 하드코딩 수동 샘플 공연 데이터 전면 삭제 (0건의 완전 깨끗한 DB 상태 유지)
    }
}
