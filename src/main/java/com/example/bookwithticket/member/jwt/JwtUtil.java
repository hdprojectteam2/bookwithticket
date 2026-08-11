package com.example.bookwithticket.member.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs
    ) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT_SECRET은 최소 32자 이상이어야 합니다.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String createToken(String email) {
        return createToken(email, null);
    }

    // 기존 dev MemberService/다른 팀 코드 호환
    public String createToken(String email, String role) {
        Date now = new Date();
        var builder = Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs));

        if (role != null && !role.isBlank()) {
            builder.claim("role", role);
        }

        return builder.signWith(key).compact();
    }

    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // 기존 호출부 호환. 인증 필터는 DB role을 우선 사용한다.
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
