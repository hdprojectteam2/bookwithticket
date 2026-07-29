package com.example.bookwithticket.member.service;

import com.example.bookwithticket.member.dto.LoginRequestDto;
import com.example.bookwithticket.member.dto.MemberRequestDto;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.exception.DuplicateEmailException;
import com.example.bookwithticket.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.bookwithticket.member.exception.LoginFailedException;
import com.example.bookwithticket.member.jwt.JwtUtil;

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

        System.out.println("=== 회원가입 실행 ===");

        String encoded = passwordEncoder.encode(requestDto.getPassword());

        System.out.println("원본 : " + requestDto.getPassword());
        System.out.println("암호화 : " + encoded);


        // 이메일 중복 체크
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new DuplicateEmailException("이미 가입된 이메일입니다.");
        }


        Member member = new Member();

        member.setEmail(requestDto.getEmail());

        // 비밀번호 암호화 저장
        member.setPassword(
                passwordEncoder.encode(requestDto.getPassword())
        );

        member.setName(requestDto.getName());


        // 기본 회원 설정
        member.setRole("USER");
        member.setActive(true);


        return memberRepository.save(member);
    }


    // 회원 조회
    public Member findById(Long id) {

        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    }


    // 로그인
    public String login(LoginRequestDto requestDto) {

        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() ->
                        new LoginFailedException("이메일 또는 비밀번호가 틀렸습니다.")
                );

        // 암호화된 비밀번호 비교
        if (!passwordEncoder.matches(
                requestDto.getPassword(),
                member.getPassword()
        )) {
            throw new LoginFailedException("이메일 또는 비밀번호가 틀렸습니다.");
        }


        return jwtUtil.createToken(member.getEmail());
    }
}