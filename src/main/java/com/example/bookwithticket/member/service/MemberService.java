package com.example.bookwithticket.member.service;

import com.example.bookwithticket.member.dto.LoginRequestDto;
import com.example.bookwithticket.member.dto.MemberRequestDto;
import com.example.bookwithticket.member.dto.MemberUpdateRequestDto;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.exception.DuplicateEmailException;
import com.example.bookwithticket.member.exception.LoginFailedException;
import com.example.bookwithticket.member.exception.MemberNotFoundException;
import com.example.bookwithticket.member.jwt.JwtUtil;
import com.example.bookwithticket.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
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

    @Transactional
    public Member signup(MemberRequestDto requestDto) {
        String email = normalizeEmail(requestDto.getEmail());

        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("이미 가입된 이메일입니다.");
        }

        Member member = Member.createLocalMember(
                email,
                passwordEncoder.encode(requestDto.getPassword()),
                requestDto.getName().trim(),
                normalizeNullable(requestDto.getPhone()),
                normalizeNullable(requestDto.getZipcode()),
                normalizeNullable(requestDto.getAddress()),
                normalizeNullable(requestDto.getDetailAddress()),
                requestDto.isMarketingAgree()
        );

        return memberRepository.save(member);
    }

    @Transactional
    public String login(LoginRequestDto requestDto) {
        String email = normalizeEmail(requestDto.getEmail());

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new LoginFailedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!member.isActive()) {
            throw new LoginFailedException("탈퇴한 회원입니다.");
        }

        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new LoginFailedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        member.recordLogin();

        return jwtUtil.createToken(member.getEmail(), member.getRole());
    }

    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(normalizeEmail(email));
    }

    public long countActiveMembers() {
        return memberRepository.countActiveMembers();
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .filter(Member::isActive)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다."));
    }

    public Member findMyInfo(String email) {
        return getActiveMember(email);
    }

    @Transactional
    public Member update(String email, MemberUpdateRequestDto requestDto) {
        Member member = getActiveMember(email);

        String encodedPassword = null;
        if (requestDto.getPassword() != null && !requestDto.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        }

        member.updateProfile(
                trimNullable(requestDto.getName()),
                encodedPassword,
                normalizeNullable(requestDto.getPhone()),
                normalizeNullable(requestDto.getZipcode()),
                normalizeNullable(requestDto.getAddress()),
                normalizeNullable(requestDto.getDetailAddress()),
                requestDto.getMarketingAgree()
        );

        return member;
    }

    @Transactional
    public void withdraw(String email) {
        Member member = getActiveMember(email);
        member.withdraw();
    }

    private Member getActiveMember(String email) {
        Member member = memberRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다."));

        if (!member.isActive()) {
            throw new MemberNotFoundException("회원을 찾을 수 없습니다.");
        }

        return member;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private String trimNullable(String value) {
        return value == null ? null : value.trim();
    }
}
