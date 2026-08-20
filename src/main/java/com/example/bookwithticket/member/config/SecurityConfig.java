package com.example.bookwithticket.member.config;

import com.example.bookwithticket.member.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // 회원 공개 API
                        .requestMatchers(
                                "/members/signup",
                                "/members/login",
                                "/members/check-email",
                                "/members/count"
                        ).permitAll()

                        // 도서 조회 공개
                        .requestMatchers(HttpMethod.GET, "/books", "/books/**")
                        .permitAll()

                        // 리뷰 작성/삭제는 로그인 회원
                        .requestMatchers(HttpMethod.POST, "/books/*/reviews")
                        .authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/reviews/**")
                        .authenticated()

                        // 도서 변경/알라딘 적재 ADMIN
                        .requestMatchers(HttpMethod.POST, "/books/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/books/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/books/**")
                        .hasRole("ADMIN")

                        // 나머지 회원 기능 로그인 필요
                        .requestMatchers("/members/**")
                        .authenticated()

                        // 공연 예매/선점 및 주문 결제 API 로그인(인증) 필수 적용 (원천 401 Unauthorized 보장)
                        .requestMatchers(
                                "/api/reservations/**",
                                "/api/orders/**",
                                "/api/payments/**",
                                "/api/refunds/**"
                        ).authenticated()

                        .requestMatchers("/admin.html", "/js/admin.js").permitAll()


                        // dev의 다른 팀 기능 정책은 현재처럼 유지
                        .anyRequest()
                        .permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
