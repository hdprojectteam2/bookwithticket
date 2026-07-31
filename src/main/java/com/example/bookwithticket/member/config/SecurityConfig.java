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
                .csrf(csrf -> csrf.disable())


                .authorizeHttpRequests(auth -> auth

                        // 회원가입
                        .requestMatchers("/members/signup").permitAll()

                        // 로그인
                        .requestMatchers("/members/login").permitAll()

                        // 도서 조회 (현재는 열어둠)
                        .requestMatchers("/books/**").permitAll()


                        // 나머지는 로그인 필요
                        .anyRequest().authenticated()
                )


                // JWT 필터 연결
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}