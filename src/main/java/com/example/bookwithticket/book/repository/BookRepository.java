package com.example.bookwithticket.book.repository;


import com.example.bookwithticket.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;



public interface BookRepository extends JpaRepository<Book, Long> {


    // 도서 제목 검색

    List<Book> findByTitleContaining(
            String keyword
    );



    // 자동완성 검색
    // 제목에 포함되는 도서 최대 5개

    List<Book> findTop5ByTitleContaining(
            String keyword
    );



    // 카테고리 조회

    List<Book> findByCategory(
            String category
    );



    // 인기 도서 TOP 10

    List<Book> findTop10ByOrderByViewCountDesc();



    // 최신 도서 TOP 10

    List<Book> findTop10ByOrderByCreatedAtDesc();


}