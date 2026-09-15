package com.example.bookwithticket.member.controller;

import com.example.bookwithticket.member.dto.*;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;
import com.example.bookwithticket.member.service.RecentBookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
@Validated
public class MemberController {

    private final MemberService memberService;
    private final RecentBookService recentBookService;

    public MemberController(
            MemberService memberService,
            RecentBookService recentBookService
    ) {
        this.memberService = memberService;
        this.recentBookService = recentBookService;
    }

    @PostMapping("/signup")
    public ResponseEntity<MemberResponseDto> signup(
            @Valid @RequestBody MemberRequestDto requestDto
    ) {
        Member member = memberService.signup(requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MemberResponseDto(member));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                new LoginResponseDto(memberService.login(requestDto))
        );
    }

    @GetMapping("/check-email")
    public ResponseEntity<EmailCheckResponseDto> checkEmail(
            @RequestParam(name = "email") @Email(message = "이메일 형식이 올바르지 않습니다.") String email
    ) {
        return ResponseEntity.ok(
                new EmailCheckResponseDto(memberService.isEmailAvailable(email))
        );
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countMembers() {
        return ResponseEntity.ok(memberService.countActiveMembers());
    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponseDto> myInfo(Authentication authentication) {
        Member member = memberService.findMyInfo(authentication.getName());
        return ResponseEntity.ok(new MemberResponseDto(member));
    }

    @GetMapping("/mypage")
    public ResponseEntity<MyPageResponseDto> myPage(Authentication authentication) {
        Member member = memberService.findMyInfo(authentication.getName());

        List<RecentBookResponseDto> recentBooks = recentBookService.findAll(member)
                .stream()
                .map(RecentBookResponseDto::new)
                .toList();

        return ResponseEntity.ok(new MyPageResponseDto(member, recentBooks));
    }

    @PutMapping("/me")
    public ResponseEntity<MemberResponseDto> update(
            Authentication authentication,
            @Valid @RequestBody MemberUpdateRequestDto requestDto
    ) {
        Member member = memberService.update(authentication.getName(), requestDto);
        return ResponseEntity.ok(new MemberResponseDto(member));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(Authentication authentication) {
        memberService.withdraw(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
