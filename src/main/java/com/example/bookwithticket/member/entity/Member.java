package com.example.bookwithticket.member.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    private String phone;
    private String zipcode;
    private String address;
    private String detailAddress;

    // dev/팀 코드 호환을 위해 String 유지
    @Column(nullable = false, length = 20)
    private String role = "USER";

    private String provider;
    private String providerId;

    @Column(nullable = false)
    private boolean marketingAgree;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private boolean isActive = true;

    public Member() {}

    public static Member createLocalMember(
            String email, String encodedPassword, String name,
            String phone, String zipcode, String address,
            String detailAddress, boolean marketingAgree
    ) {
        Member member = new Member();
        member.email = email;
        member.password = encodedPassword;
        member.name = name;
        member.phone = phone;
        member.zipcode = zipcode;
        member.address = address;
        member.detailAddress = detailAddress;
        member.marketingAgree = marketingAgree;
        member.role = "USER";
        member.isActive = true;
        return member;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.role == null || this.role.isBlank()) this.role = "USER";
        this.isActive = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void recordLogin() { this.lastLoginAt = LocalDateTime.now(); }

    public void updateProfile(
            String name, String encodedPassword, String phone,
            String zipcode, String address, String detailAddress,
            Boolean marketingAgree
    ) {
        if (name != null) this.name = name;
        if (encodedPassword != null) this.password = encodedPassword;
        if (phone != null) this.phone = phone;
        if (zipcode != null) this.zipcode = zipcode;
        if (address != null) this.address = address;
        if (detailAddress != null) this.detailAddress = detailAddress;
        if (marketingAgree != null) this.marketingAgree = marketingAgree;
    }

    public void withdraw() { this.isActive = false; }

    // 기존 dev/팀 코드 호환 getter/setter 유지
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDetailAddress() { return detailAddress; }
    public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public boolean isMarketingAgree() { return marketingAgree; }
    public void setMarketingAgree(boolean marketingAgree) { this.marketingAgree = marketingAgree; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
