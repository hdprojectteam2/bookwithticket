package com.example.bookwithticket.member.controller;


import com.example.bookwithticket.member.dto.LoginRequestDto;
import com.example.bookwithticket.member.dto.MemberRequestDto;
import com.example.bookwithticket.member.dto.MemberUpdateRequestDto;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.repository.MemberRepository;
import com.example.bookwithticket.member.service.MemberService;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;



@RestController
@RequestMapping("/members")
public class MemberController {



    private final MemberService memberService;

    private final MemberRepository memberRepository;



    public MemberController(
            MemberService memberService,
            MemberRepository memberRepository
    ){

        this.memberService = memberService;
        this.memberRepository = memberRepository;

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


        String email =
                authentication.getName();



        Member member =
                memberService.findMyInfo(email);



        return ResponseEntity.ok(member);


    }







    // 회원정보 수정

    @PutMapping("/me")
    public ResponseEntity<?> update(

            Authentication authentication,

            @RequestBody MemberUpdateRequestDto requestDto

    ){


        String email =
                authentication.getName();



        Member member =
                memberService.update(
                        email,
                        requestDto
                );



        return ResponseEntity.ok(member);


    }







    // 회원 탈퇴

    @DeleteMapping("/me")
    public ResponseEntity<?> delete(

            Authentication authentication

    ){


        String email =
                authentication.getName();



        Member member =
                memberService.delete(email);



        return ResponseEntity.ok(member);


    }


}