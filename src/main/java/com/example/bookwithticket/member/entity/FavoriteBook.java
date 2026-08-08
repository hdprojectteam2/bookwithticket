package com.example.bookwithticket.member.entity;


import com.example.bookwithticket.book.entity.Book;

import jakarta.persistence.*;

import java.time.LocalDateTime;



@Entity
public class FavoriteBook {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @ManyToOne
    @JoinColumn(name="member_id")
    private Member member;



    @ManyToOne
    @JoinColumn(name="book_id")
    private Book book;



    private LocalDateTime createdAt;




    public FavoriteBook(){

    }



    public FavoriteBook(
            Member member,
            Book book
    ){

        this.member = member;
        this.book = book;
        this.createdAt = LocalDateTime.now();

    }




    public Long getId(){

        return id;

    }



    public Member getMember(){

        return member;

    }



    public Book getBook(){

        return book;

    }



    public LocalDateTime getCreatedAt(){

        return createdAt;

    }

}