package com.example.bookwithticket.member.controller;


import com.example.bookwithticket.member.dto.RecentBookResponseDto;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;
import com.example.bookwithticket.member.service.RecentBookService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/members")
public class RecentBookController {



    private final RecentBookService recentBookService;

    private final MemberService memberService;



    public RecentBookController(
            RecentBookService recentBookService,
            MemberService memberService
    ){

        this.recentBookService = recentBookService;
        this.memberService = memberService;

    }




    @GetMapping("/recent-books")
    public List<RecentBookResponseDto> recentBooks(
            Authentication authentication
    ){


        if(authentication == null){

            throw new RuntimeException(
                    "로그인이 필요합니다."
            );

        }


        Member member =
                memberService.findMyInfo(
                        authentication.getName()
                );



        return recentBookService
                .findAll(member)

                .stream()

                .map(RecentBookResponseDto::new)

                .toList();

    }


}