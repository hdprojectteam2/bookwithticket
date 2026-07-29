package com.example.bookwithticket.member.controller;

import com.example.bookwithticket.member.dto.MemberRequestDto;
import com.example.bookwithticket.member.dto.MemberResponseDto;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;
import org.springframework.web.bind.annotation.*;
import com.example.bookwithticket.member.dto.LoginRequestDto;
import com.example.bookwithticket.member.dto.LoginResponseDto;
import org.springframework.security.core.Authentication;

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

}