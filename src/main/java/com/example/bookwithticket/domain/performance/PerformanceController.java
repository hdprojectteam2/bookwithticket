package com.example.bookwithticket.domain.performance;

import com.example.bookwithticket.global.common.ApiResponse;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.jwt.JwtUtil;
import com.example.bookwithticket.member.repository.MemberRepository;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.bookwithticket.global.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/performances")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final KopisPerformanceImportService kopisImportService;

    public PerformanceController(
            PerformanceService performanceService,
            MemberRepository memberRepository,
            JwtUtil jwtUtil,
            KopisPerformanceImportService kopisImportService) {
        this.performanceService = performanceService;
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
        this.kopisImportService = kopisImportService;
    }

    // 임시 테스트용 토큰 발급 API (type: USER / ADMIN)
    @GetMapping("/test-token")
    public ApiResponse<Map<String, String>> getTestToken(
            @RequestParam(value = "type", required = false, defaultValue = "USER") String type) {
        String targetEmail = "ADMIN".equalsIgnoreCase(type) ? "admin@example.com" : "test@example.com";
        Member member = memberRepository.findByEmail(targetEmail)
                .orElseGet(() -> {
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                    boolean isAdmin = "ADMIN".equalsIgnoreCase(type);
                    Member m = Member.createLocalMember(
                            targetEmail,
                            encoder.encode(isAdmin ? "admin123" : "password123"),
                            isAdmin ? "관리자" : "일반사용자",
                            isAdmin ? "010-9876-5432" : "010-1234-5678",
                            "12345",
                            "서울시",
                            "101호",
                            true
                    );
                    m.setRole(isAdmin ? "ADMIN" : "USER");
                    return memberRepository.save(m);
                });

        String token = jwtUtil.createToken(member.getEmail(), member.getRole());
        return ApiResponse.ok(Map.of(
                "token", token,
                "email", member.getEmail(),
                "name", member.getName(),
                "role", member.getRole()
        ));
    }

    // 관리자 권한 검증 메소드
    private void checkAdmin(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
        if (!"ADMIN".equalsIgnoreCase(member.getRole())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    //2. 검색시 도착
    @GetMapping
    public ApiResponse<List<PerformanceResponse>> getPerformances(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "includeInactive", required = false, defaultValue = "false") boolean includeInactive) {
        PerformanceCategory categoryEnum = null;
        if (category != null && !category.isBlank()) {
            try {
                categoryEnum = PerformanceCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                categoryEnum = null;
            }
        }
        return ApiResponse.ok(performanceService.getPerformances(keyword, categoryEnum, includeInactive));
    }

    //상세보기 
    @GetMapping("/{id}")
    public ApiResponse<PerformanceResponse> getPerformance(
            @PathVariable("id") Long id,
            Authentication authentication) {
        boolean allowInactive = false;
        if (authentication != null && authentication.isAuthenticated()) {
            Member member = memberRepository.findByEmail(authentication.getName()).orElse(null);
            if (member != null && "ADMIN".equalsIgnoreCase(member.getRole())) {
                allowInactive = true;
            }
        }
        return ApiResponse.ok(performanceService.getPerformance(id, allowInactive));
    }

    //회차조회
    @GetMapping("/{id}/schedules")
    public ApiResponse<List<ScheduleResponse>> getSchedules(@PathVariable("id") Long id) {
        return ApiResponse.ok(performanceService.getSchedules(id));
    }

    // 관리자: 공연 신규 등록
    @PostMapping
    public ApiResponse<PerformanceResponse> createPerformance(
            Authentication authentication,
            @jakarta.validation.Valid @RequestBody PerformanceRequestDto dto) {
        checkAdmin(authentication);
        return ApiResponse.ok("공연이 등록되었습니다.", performanceService.createPerformance(dto));
    }

    // 관리자: 공연 정보 수정
    @PutMapping("/{id}")
    public ApiResponse<PerformanceResponse> updatePerformance(
            Authentication authentication,
            @PathVariable("id") Long id,
            @jakarta.validation.Valid @RequestBody PerformanceRequestDto dto) {
        checkAdmin(authentication);
        return ApiResponse.ok("공연 정보가 수정되었습니다.", performanceService.updatePerformance(id, dto));
    }

    // 관리자: 공연 비활성화 (Soft Delete)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePerformance(
            Authentication authentication,
            @PathVariable("id") Long id) {
        checkAdmin(authentication);
        performanceService.deletePerformance(id);
        return ApiResponse.ok();
    }

    // 관리자: 공연 재활성화 (복구)
    @PatchMapping("/{id}/activate")
    public ApiResponse<Void> activatePerformance(
            Authentication authentication,
            @PathVariable("id") Long id) {
        checkAdmin(authentication);
        performanceService.activatePerformance(id);
        return ApiResponse.ok();
    }

    // 관리자: 공연 영구 완전 삭제 (Hard Delete)
    @DeleteMapping("/{id}/hard")
    public ApiResponse<Void> hardDeletePerformance(
            Authentication authentication,
            @PathVariable("id") Long id) {
        checkAdmin(authentication);
        performanceService.hardDeletePerformance(id);
        return ApiResponse.ok();
    }

    // 관리자: 회차 및 좌석 자동 생성
    @PostMapping({ "/{id}/schedules", "/admin/{id}/schedules" })
    public ApiResponse<ScheduleResponse> createScheduleAndSeats(
            Authentication authentication,
            @PathVariable("id") Long id,
            @jakarta.validation.Valid @RequestBody ScheduleCreateRequest dto) {
        checkAdmin(authentication);
        return ApiResponse.ok("회차 및 좌석이 생성되었습니다.", performanceService.createScheduleAndSeats(id, dto));
    }

    // 관리자: KOPIS 공연 목록 검색 (적재 전 후보 조회)
    @GetMapping("/admin/search/kopis")
    public ApiResponse<List<com.example.bookwithticket.domain.performance.api.KopisPerformanceResponse.Item>> searchKopis(
            Authentication authentication,
            @RequestParam(value = "keyword", required = false) String keyword) {
        checkAdmin(authentication);
        return ApiResponse.ok(kopisImportService.searchKopis(keyword));
    }

    // 관리자: 선택한 KOPIS 공연들만 DB 적재
    @PostMapping("/admin/import/kopis/select")
    public ApiResponse<List<PerformanceResponse>> importSelectedFromKopis(
            Authentication authentication,
            @RequestBody List<com.example.bookwithticket.domain.performance.api.KopisPerformanceResponse.Item> selectedItems) {
        checkAdmin(authentication);
        return ApiResponse.ok("선택한 KOPIS 공연 데이터가 성공적으로 적재되었습니다.", kopisImportService.importSelectedItems(selectedItems));
    }
}
