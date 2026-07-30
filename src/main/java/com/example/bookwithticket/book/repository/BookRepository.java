package com.example.bookwithticket.book.repository;

import com.example.bookwithticket.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

}