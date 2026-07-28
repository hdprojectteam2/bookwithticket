package com.example.bookwithticket.member.service;

import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import com.example.bookwithticket.member.dto.MemberRequestDto;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member save(MemberRequestDto requestDto) {

        Member member = new Member();

        member.setEmail(requestDto.getEmail());
        member.setPassword(requestDto.getPassword());
        member.setName(requestDto.getName());

        return memberRepository.save(member);
    }

}