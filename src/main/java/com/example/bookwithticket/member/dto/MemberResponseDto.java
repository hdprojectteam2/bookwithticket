package com.example.bookwithticket.member.dto;

import com.example.bookwithticket.member.entity.Member;

import java.time.LocalDateTime;

public class MemberResponseDto {

    private final Long id;
    private final String email;
    private final String name;
    private final String phone;
    private final String zipcode;
    private final String address;
    private final String detailAddress;
    private final String role;
    private final boolean marketingAgree;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastLoginAt;

    public MemberResponseDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.phone = member.getPhone();
        this.zipcode = member.getZipcode();
        this.address = member.getAddress();
        this.detailAddress = member.getDetailAddress();
        this.role = member.getRole();
        this.marketingAgree = member.isMarketingAgree();
        this.createdAt = member.getCreatedAt();
        this.lastLoginAt = member.getLastLoginAt();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getZipcode() { return zipcode; }
    public String getAddress() { return address; }
    public String getDetailAddress() { return detailAddress; }
    public String getRole() { return role; }
    public boolean isMarketingAgree() { return marketingAgree; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
}
