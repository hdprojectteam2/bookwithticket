package com.example.bookwithticket.member.service;


import com.example.bookwithticket.member.dto.LoginRequestDto;
import com.example.bookwithticket.member.dto.MemberRequestDto;
import com.example.bookwithticket.member.dto.MemberUpdateRequestDto;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.exception.DuplicateEmailException;
import com.example.bookwithticket.member.exception.LoginFailedException;
import com.example.bookwithticket.member.jwt.JwtUtil;
import com.example.bookwithticket.member.repository.MemberRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;



@Service
public class MemberService {


    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;



    public MemberService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {

        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;

    }




    // 회원가입
    public Member save(MemberRequestDto requestDto) {



        if(memberRepository.findByEmail(requestDto.getEmail()).isPresent()){

            throw new DuplicateEmailException(
                    "이미 가입된 이메일입니다."
            );

        }



        Member member = new Member();


        member.setEmail(
                requestDto.getEmail()
        );


        member.setPassword(
                passwordEncoder.encode(
                        requestDto.getPassword()
                )
        );


        member.setName(
                requestDto.getName()
        );



        // 추가 회원 정보

        member.setPhone(
                requestDto.getPhone()
        );


        member.setZipcode(
                requestDto.getZipcode()
        );


        member.setAddress(
                requestDto.getAddress()
        );


        member.setDetailAddress(
                requestDto.getDetailAddress()
        );


        member.setMarketingAgree(
                requestDto.isMarketingAgree()
        );



        // 기본 설정

        member.setRole("USER");

        member.setActive(true);



        return memberRepository.save(member);

    }






    // 회원 조회

    public Member findById(Long id){


        return memberRepository.findById(id)

                .orElseThrow(() ->
                        new RuntimeException(
                                "회원을 찾을 수 없습니다."
                        )
                );

    }






    // 로그인

    public String login(LoginRequestDto requestDto){


        Member member =
                memberRepository.findByEmail(
                                requestDto.getEmail()
                        )

                        .orElseThrow(() ->
                                new LoginFailedException(
                                        "이메일 또는 비밀번호가 틀렸습니다."
                                )
                        );



        if(!member.isActive()){

            throw new LoginFailedException(
                    "탈퇴한 회원입니다."
            );

        }




        if(!passwordEncoder.matches(
                requestDto.getPassword(),
                member.getPassword()
        )){


            throw new LoginFailedException(
                    "이메일 또는 비밀번호가 틀렸습니다."
            );


        }




        // 마지막 로그인 시간 저장

        member.setLastLoginAt(
                LocalDateTime.now()
        );


        memberRepository.save(member);




        return jwtUtil.createToken(
                member.getEmail()
        );


    }







    // 내 정보 조회

    public Member findMyInfo(String email){


        return memberRepository.findByEmail(email)

                .orElseThrow(() ->
                        new LoginFailedException(
                                "회원을 찾을 수 없습니다."
                        )
                );

    }






    // 회원 정보 수정

    public Member update(
            String email,
            MemberUpdateRequestDto requestDto
    ){


        Member member =
                memberRepository.findByEmail(email)

                        .orElseThrow(() ->
                                new LoginFailedException(
                                        "회원을 찾을 수 없습니다."
                                )
                        );



        if(requestDto.getName()!=null){

            member.setName(
                    requestDto.getName()
            );

        }



        if(requestDto.getPassword()!=null){

            member.setPassword(
                    passwordEncoder.encode(
                            requestDto.getPassword()
                    )
            );

        }



        if(requestDto.getPhone()!=null){

            member.setPhone(
                    requestDto.getPhone()
            );

        }



        if(requestDto.getZipcode()!=null){

            member.setZipcode(
                    requestDto.getZipcode()
            );

        }



        if(requestDto.getAddress()!=null){

            member.setAddress(
                    requestDto.getAddress()
            );

        }



        if(requestDto.getDetailAddress()!=null){

            member.setDetailAddress(
                    requestDto.getDetailAddress()
            );

        }



        return memberRepository.save(member);


    }






    // 회원 탈퇴

    public Member delete(String email){


        Member member =
                memberRepository.findByEmail(email)

                        .orElseThrow(() ->
                                new LoginFailedException(
                                        "회원을 찾을 수 없습니다."
                                )
                        );



        member.setActive(false);



        return memberRepository.save(member);


    }


}