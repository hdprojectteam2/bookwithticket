package com.example.bookwithticket.book.repository;

import com.example.bookwithticket.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {


    List<Book> findByTitleContainingOrAuthorContaining(
            String title,
            String author
    );

}