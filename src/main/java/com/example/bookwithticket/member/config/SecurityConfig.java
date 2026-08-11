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
                                "/members/check-email"
                        ).permitAll()

                        // 도서 조회 공개
                        .requestMatchers(HttpMethod.GET, "/books", "/books/**")
                        .permitAll()

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

                        .requestMatchers("/admin.html", "/js/admin.js")
                        .hasRole("ADMIN")

                        // dev의 다른 팀 기능 정책은 현재처럼 유지
                        .anyRequest()
                        .permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
