package com.example.bookwithticket.member.dto;


import com.example.bookwithticket.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;



public class MyPageResponseDto {


    private String name;

    private String email;

    private String phone;

    private String zipcode;

    private String address;

    private String detailAddress;

    private LocalDateTime createdAt;


    private List<RecentBookResponseDto> recentBooks;





    public MyPageResponseDto(
            Member member,
            List<RecentBookResponseDto> recentBooks
    ){


        this.name =
                member.getName();


        this.email =
                member.getEmail();


        this.phone =
                member.getPhone();


        this.zipcode =
                member.getZipcode();


        this.address =
                member.getAddress();


        this.detailAddress =
                member.getDetailAddress();


        this.createdAt =
                member.getCreatedAt();


        this.recentBooks =
                recentBooks;


    }






    public String getName(){
        return name;
    }


    public String getEmail(){
        return email;
    }


    public String getPhone(){
        return phone;
    }


    public String getZipcode(){
        return zipcode;
    }


    public String getAddress(){
        return address;
    }


    public String getDetailAddress(){
        return detailAddress;
    }


    public LocalDateTime getCreatedAt(){
        return createdAt;
    }


    public List<RecentBookResponseDto> getRecentBooks(){
        return recentBooks;
    }

}