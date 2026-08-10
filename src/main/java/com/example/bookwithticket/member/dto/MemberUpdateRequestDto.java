package com.example.bookwithticket.member.dto;


public class MemberUpdateRequestDto {


    private String name;

    private String password;


    private String phone;

    private String zipcode;

    private String address;

    private String detailAddress;



    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
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

}