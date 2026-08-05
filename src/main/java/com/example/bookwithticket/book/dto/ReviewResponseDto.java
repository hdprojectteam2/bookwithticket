package com.example.bookwithticket.book.dto;



import com.example.bookwithticket.book.entity.Review;



import java.time.LocalDateTime;



public class ReviewResponseDto {



    private Long id;


    private String name;


    private String content;


    private int rating;


    private LocalDateTime createdAt;







    public ReviewResponseDto(
            Review review
    ){


        this.id =
                review.getId();



        this.name =
                review.getMember()
                        .getName();



        this.content =
                review.getContent();



        this.rating =
                review.getRating();



        this.createdAt =
                review.getCreatedAt();


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


}