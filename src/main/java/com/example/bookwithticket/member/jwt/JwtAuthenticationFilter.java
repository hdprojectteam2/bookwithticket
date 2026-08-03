package com.example.bookwithticket.member.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;



@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtUtil jwtUtil;


    public JwtAuthenticationFilter(
            JwtUtil jwtUtil
    ) {

        this.jwtUtil = jwtUtil;

    }



    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        String header = request.getHeader("Authorization");



        if (header != null && header.startsWith("Bearer ")) {


            String token = header.substring(7);



            try {


                String email = jwtUtil.getEmail(token);



                // 인증 정보가 없는 경우만 저장
                if (SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null) {


                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    List.of(
                                            new SimpleGrantedAuthority(
                                                    "ROLE_USER"
                                            )
                                    )
                            );



                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);

                }



            } catch (Exception e) {


                // 잘못된 토큰이면 인증 제거
                SecurityContextHolder
                        .clearContext();

            }

        }



        filterChain.doFilter(
                request,
                response
        );

    }

}