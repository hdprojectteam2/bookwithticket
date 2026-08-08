package com.example.bookwithticket.member.repository;


import com.example.bookwithticket.member.entity.FavoriteBook;
import com.example.bookwithticket.member.entity.Member;

import com.example.bookwithticket.book.entity.Book;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;



public interface FavoriteBookRepository
        extends JpaRepository<FavoriteBook, Long>{



    List<FavoriteBook>
    findTop10ByMemberOrderByCreatedAtDesc(
            Member member
    );



    Optional<FavoriteBook>
    findByMemberAndBook(
            Member member,
            Book book
    );


}