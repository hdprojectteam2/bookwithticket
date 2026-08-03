package com.example.bookwithticket.member.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true)
    private String email;


    private String password;

    private String name;


    private String phone;

    private String zipcode;

    private String address;

    private String detailAddress;


    private String role;


    private String provider;

    private String providerId;


    private boolean marketingAgree;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;


    private boolean isActive;


    public Member() {

    }


    @PrePersist
    public void prePersist(){

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

    }


    @PreUpdate
    public void preUpdate(){

        this.updatedAt = LocalDateTime.now();

    }



    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getZipcode() {
        return zipcode;
    }


    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }


    public String getAddress() {
        return address;
    }


    public void setAddress(String address) {
        this.address = address;
    }


    public String getDetailAddress() {
        return detailAddress;
    }


    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }


    public String getRole() {
        return role;
    }


    public void setRole(String role) {
        this.role = role;
    }


    public String getProvider() {
        return provider;
    }


    public void setProvider(String provider) {
        this.provider = provider;
    }


    public String getProviderId() {
        return providerId;
    }


    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }


    public boolean isMarketingAgree() {
        return marketingAgree;
    }


    public void setMarketingAgree(boolean marketingAgree) {
        this.marketingAgree = marketingAgree;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }


    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }


    public boolean isActive() {
        return isActive;
    }


    public void setActive(boolean active) {
        isActive = active;
    }

}