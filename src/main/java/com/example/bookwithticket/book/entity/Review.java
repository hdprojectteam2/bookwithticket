package com.example.bookwithticket.book.entity;


import com.example.bookwithticket.member.entity.Member;

import jakarta.persistence.*;

import java.time.LocalDateTime;



@Entity
public class Review {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;



    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;



    @Column(nullable = false)
    private String content;



    @Column(nullable = false)
    private int rating;



    private LocalDateTime createdAt;





    public Review(){

    }





    public Review(
            Member member,
            Book book,
            String content,
            int rating
    ){

        this.member = member;

        this.book = book;

        this.content = content;

        this.rating = rating;

        this.createdAt =
                LocalDateTime.now();

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



    public String getContent(){

        return content;

    }



    public int getRating(){

        return rating;

    }

    // 리뷰 수정
    public void update(
            String content,
            int rating
    ) {
        this.content = content;
        this.rating = rating;
    }




    public LocalDateTime getCreatedAt(){

        return createdAt;

    }

}