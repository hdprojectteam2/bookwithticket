package com.example.bookwithticket.member.config;


import com.example.bookwithticket.member.jwt.JwtAuthenticationFilter;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.http.HttpMethod;


import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration
public class SecurityConfig {



    private final JwtAuthenticationFilter jwtAuthenticationFilter;




    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ){

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

    }








    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {



        http


                .csrf(csrf ->
                        csrf.disable()
                )



                .formLogin(form ->
                        form.disable()
                )



                .logout(logout ->
                        logout.disable()
                )



                .authorizeHttpRequests(auth -> auth





                        // 회원가입

                        .requestMatchers(
                                "/members/signup"
                        )
                        .permitAll()






                        // 로그인

                        .requestMatchers(
                                "/members/login"
                        )
                        .permitAll()






                        // ======================
                        // 도서 조회
                        // ======================


                        .requestMatchers(
                                HttpMethod.GET,
                                "/books",
                                "/books/**"
                        )
                        .permitAll()






                        // ======================
                        // 관리자 도서 등록
                        // ======================


                        .requestMatchers(
                                HttpMethod.POST,
                                "/books"
                        )
                        .hasRole("ADMIN")






                        // ======================
                        // 관리자 도서 수정
                        // ======================


                        .requestMatchers(
                                HttpMethod.PUT,
                                "/books/**"
                        )
                        .hasRole("ADMIN")







                        // ======================
                        // 관리자 도서 삭제
                        // ======================


                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/books/**"
                        )
                        .hasRole("ADMIN")








                        // 회원 정보

                        .requestMatchers(
                                "/members/me"
                        )
                        .authenticated()







                        // 관리자 페이지

                        .requestMatchers(
                                "/admin.html",
                                "/js/admin.js"
                        )
                        .hasRole("ADMIN")







                        .anyRequest()
                        .permitAll()



                )





                .addFilterBefore(

                        jwtAuthenticationFilter,

                        UsernamePasswordAuthenticationFilter.class

                );





        return http.build();

    }



}