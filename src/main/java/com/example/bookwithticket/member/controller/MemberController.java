package com.example.bookwithticket.member.controller;

import com.example.bookwithticket.member.dto.MemberRequestDto;
import com.example.bookwithticket.member.dto.MemberResponseDto;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;
import org.springframework.web.bind.annotation.*;

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


    @GetMapping("/members/{id}")
    public MemberResponseDto findMember(@PathVariable Long id) {

        Member member = memberService.findById(id);

        return new MemberResponseDto(member);
    }

}