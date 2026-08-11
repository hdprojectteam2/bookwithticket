package com.example.bookwithticket.member.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MemberUpdateRequestDto {

    @Size(min = 1, max = 50, message = "이름은 1~50자로 입력해주세요.")
    private String name;

    @Size(min = 8, max = 64, message = "비밀번호는 8~64자로 입력해주세요.")
    private String password;

    @Pattern(regexp = "^$|^[0-9-]{9,20}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phone;

    @Size(max = 10)
    private String zipcode;

    @Size(max = 255)
    private String address;

    @Size(max = 255)
    private String detailAddress;

    private Boolean marketingAgree;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDetailAddress() { return detailAddress; }
    public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
    public Boolean getMarketingAgree() { return marketingAgree; }
    public void setMarketingAgree(Boolean marketingAgree) { this.marketingAgree = marketingAgree; }
}
