package com.example.bookwithticket.member.controller;

import com.example.bookwithticket.member.dto.MemberRequestDto;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/members/signup")
    public Member signup(@RequestBody MemberRequestDto requestDto) {
        return memberService.save(requestDto);
    }
}