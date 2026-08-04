package com.example.bookwithticket.member.controller;


import com.example.bookwithticket.member.dto.LoginRequestDto;
import com.example.bookwithticket.member.dto.MemberRequestDto;
import com.example.bookwithticket.member.dto.MemberUpdateRequestDto;
import com.example.bookwithticket.member.dto.MyPageResponseDto;
import com.example.bookwithticket.member.dto.RecentBookResponseDto;


import com.example.bookwithticket.member.entity.Member;


import com.example.bookwithticket.member.repository.MemberRepository;


import com.example.bookwithticket.member.service.MemberService;
import com.example.bookwithticket.member.service.RecentBookService;


import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;





@RestController
@RequestMapping("/members")
public class MemberController {



    private final MemberService memberService;


    private final MemberRepository memberRepository;


    private final RecentBookService recentBookService;





    public MemberController(
            MemberService memberService,
            MemberRepository memberRepository,
            RecentBookService recentBookService
    ){


        this.memberService = memberService;

        this.memberRepository = memberRepository;

        this.recentBookService = recentBookService;

    }









    // 회원가입

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody MemberRequestDto requestDto
    ){


        Member member =
                memberService.save(requestDto);


        return ResponseEntity.ok(member);

    }








    // 로그인

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDto requestDto
    ){


        String token =
                memberService.login(requestDto);



        return ResponseEntity.ok(
                Map.of(
                        "token",
                        token
                )
        );

    }








    // 이메일 중복 확인

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(
            @RequestParam String email
    ){


        boolean exists =
                memberRepository.findByEmail(email)
                        .isPresent();



        return ResponseEntity.ok(
                Map.of(
                        "available",
                        !exists
                )
        );

    }









    // 내 정보 조회

    @GetMapping("/me")
    public ResponseEntity<?> myInfo(
            Authentication authentication
    ){


        Member member =
                memberService.findMyInfo(
                        authentication.getName()
                );


        return ResponseEntity.ok(member);

    }









    // 마이페이지 조회

    @GetMapping("/mypage")
    public ResponseEntity<?> myPage(
            Authentication authentication
    ){


        Member member =
                memberService.findMyInfo(
                        authentication.getName()
                );



        List<RecentBookResponseDto> recentBooks =

                recentBookService.findAll(member)

                        .stream()

                        .map(RecentBookResponseDto::new)

                        .toList();




        MyPageResponseDto response =

                new MyPageResponseDto(
                        member,
                        recentBooks
                );



        return ResponseEntity.ok(response);

    }









    // 회원정보 수정

    @PutMapping("/me")
    public ResponseEntity<?> update(

            Authentication authentication,

            @RequestBody MemberUpdateRequestDto requestDto

    ){


        Member member =

                memberService.update(
                        authentication.getName(),
                        requestDto
                );



        return ResponseEntity.ok(member);

    }









    // 회원 탈퇴

    @DeleteMapping("/me")
    public ResponseEntity<?> delete(

            Authentication authentication

    ){


        Member member =

                memberService.delete(
                        authentication.getName()
                );



        return ResponseEntity.ok(member);

    }


}