package com.example.bookwithticket.member.config;


import com.example.bookwithticket.member.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig {


    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }



    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {


        http

                // CSRF 비활성화
                .csrf(csrf ->
                        csrf.disable()
                )


                // JWT 사용 → 기본 로그인 페이지 제거
                .formLogin(form ->
                        form.disable()
                )


                // 기본 로그아웃 비활성화
                .logout(logout ->
                        logout.disable()
                )


                .authorizeHttpRequests(auth -> auth


                        // 회원가입
                        .requestMatchers(
                                "/members/signup"
                        ).permitAll()


                        // 로그인
                        .requestMatchers(
                                "/members/login"
                        ).permitAll()


                        // 도서 조회
                        .requestMatchers(
                                "/books/**"
                        ).permitAll()


                        // 회원 정보
                        .requestMatchers(
                                "/members/me"
                        ).authenticated()


                        // 나머지 허용
                        .anyRequest().permitAll()

                )


                // JWT Filter 등록
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();

    }

}