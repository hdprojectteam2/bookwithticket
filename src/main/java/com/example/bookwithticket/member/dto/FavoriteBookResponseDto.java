package com.example.bookwithticket.member.dto;


import com.example.bookwithticket.member.entity.FavoriteBook;



public class FavoriteBookResponseDto {


    private Long id;

    private String title;

    private String author;

    private String thumbnail;




    public FavoriteBookResponseDto(
            FavoriteBook favoriteBook
    ){


        this.id =
                favoriteBook.getBook().getId();


        this.title =
                favoriteBook.getBook().getTitle();


        this.author =
                favoriteBook.getBook().getAuthor();


        this.thumbnail =
                favoriteBook.getBook().getThumbnail();

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