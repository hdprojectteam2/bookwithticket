package com.example.bookwithticket.member.controller;

import com.example.bookwithticket.member.dto.MemberRequestDto;
import com.example.bookwithticket.member.dto.MemberResponseDto;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;
import org.springframework.web.bind.annotation.*;
import com.example.bookwithticket.member.dto.LoginRequestDto;
import com.example.bookwithticket.member.dto.LoginResponseDto;
import org.springframework.security.core.Authentication;
import com.example.bookwithticket.member.dto.MemberUpdateRequestDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }


    @PostMapping("/members/signup")
    public MemberResponseDto signup(@RequestBody MemberRequestDto requestDto) {

        Member member = memberService.save(requestDto);

        return new MemberResponseDto(member);
    }

    @GetMapping("/members/me")
    public MemberResponseDto myInfo(Authentication authentication) {

        String email = authentication.getName();

        Member member = memberService.findMyInfo(email);

        return new MemberResponseDto(member);
    }

    @GetMapping("/members/{id}")
    public MemberResponseDto findMember(@PathVariable Long id) {

        Member member = memberService.findById(id);

        return new MemberResponseDto(member);
    }


    @PostMapping("/members/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto requestDto) {

        String token = memberService.login(requestDto);

        return new LoginResponseDto(token);
    }

    @PutMapping("/members/me")
    public MemberResponseDto update(
            @RequestBody MemberUpdateRequestDto requestDto,
            Authentication authentication
    ) {

        String email = authentication.getName();

        Member member = memberService.update(
                email,
                requestDto
        );

        return new MemberResponseDto(member);
    }

    @DeleteMapping("/members/me")
    public String delete(Authentication authentication) {

        String email = authentication.getName();

        memberService.delete(email);

        return "회원 탈퇴 완료";
    }
}