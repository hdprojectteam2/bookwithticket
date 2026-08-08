package com.example.bookwithticket.member.dto;


import com.example.bookwithticket.member.entity.RecentBook;


public class RecentBookResponseDto {


    private Long id;

    private String title;

    private String author;

    private String thumbnail;



    public RecentBookResponseDto(
            RecentBook recentBook
    ){

        this.id =
                recentBook.getBook().getId();

        this.title =
                recentBook.getBook().getTitle();

        this.author =
                recentBook.getBook().getAuthor();

        this.thumbnail =
                recentBook.getBook().getThumbnail();

    }



    public Long getId(){
        return id;
    }


    public String getTitle(){
        return title;
    }


    public String getAuthor(){
        return author;
    }


    public String getThumbnail(){
        return thumbnail;
    }

}