package com.example.bookwithticket.book.dto;



import com.example.bookwithticket.book.entity.Review;
import lombok.Getter;


import java.time.LocalDateTime;


@Getter
public class ReviewResponseDto {



    private Long id;


    private String name;


    private String content;


    private int rating;


    private LocalDateTime createdAt;


    private Long memberId;

    private String email;


    public ReviewResponseDto(
            Review review
    ){


        this.id =
                review.getId();


        this.memberId =
                review.getMember()
                        .getId();


        this.name =
                review.getMember()
                        .getName();



        this.content =
                review.getContent();



        this.rating =
                review.getRating();



        this.createdAt =
                review.getCreatedAt();


        this.email =
                review.getMember()
                        .getEmail();
    }







    public Long getId(){

        return id;

    }



    public String getName(){

        return name;

    }



    public String getContent(){

        return content;

    }



    public int getRating(){

        return rating;

    }



    public LocalDateTime getCreatedAt(){

        return createdAt;

    }


    public Long getMemberId(){

        return memberId;

    }

    public String getEmail(){

        return email;

    }
}