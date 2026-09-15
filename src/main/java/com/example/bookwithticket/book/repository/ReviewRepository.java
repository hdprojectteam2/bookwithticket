package com.example.bookwithticket.book.repository;


import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.entity.Review;
import com.example.bookwithticket.member.entity.Member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;



public interface ReviewRepository
        extends JpaRepository<Review, Long> {



    // 도서 리뷰 조회

    List<Review> findByBookOrderByCreatedAtDesc(
            Book book
    );



    // 회원 리뷰 조회

    List<Review> findByMember(
            Member member
    );





    // 평균 평점

    @Query(
            "select avg(r.rating) " +
                    "from Review r " +
                    "where r.book.id = :bookId"
    )
    Double findAverageRating(
            Long bookId
    );





    // 리뷰 개수

    @Query(
            "select count(r) " +
                    "from Review r " +
                    "where r.book.id = :bookId"
    )
    long countReview(
            Long bookId
    );


}