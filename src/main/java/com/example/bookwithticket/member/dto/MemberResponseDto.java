package com.example.bookwithticket.member.dto;

import com.example.bookwithticket.member.entity.Member;

import java.time.LocalDateTime;

public class MemberResponseDto {

    private Long id;
    private String email;
    private String name;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public MemberResponseDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.role = member.getRole();
        this.createdAt = member.getCreatedAt();
        this.updatedAt = member.getUpdatedAt();
    }


    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}