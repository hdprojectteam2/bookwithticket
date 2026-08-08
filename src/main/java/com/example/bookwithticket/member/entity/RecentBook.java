package com.example.bookwithticket.member.entity;


import com.example.bookwithticket.book.entity.Book;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
public class RecentBook {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name="member_id")
    private Member member;


    @ManyToOne
    @JoinColumn(name="book_id")
    private Book book;


    private LocalDateTime viewedAt;



    public RecentBook(){

    }


    public RecentBook(
            Member member,
            Book book
    ){

        this.member = member;
        this.book = book;
        this.viewedAt = LocalDateTime.now();

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


    public LocalDateTime getViewedAt(){
        return viewedAt;
    }

}