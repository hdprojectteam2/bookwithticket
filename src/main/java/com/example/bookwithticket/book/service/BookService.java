package com.example.bookwithticket.book.service;

import com.example.bookwithticket.book.dto.BookRequestDto;
import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {


    private final BookRepository bookRepository;


    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }


    // 도서 등록
    public Book save(BookRequestDto requestDto) {


        Book book = new Book(
                requestDto.getTitle(),
                requestDto.getAuthor(),
                requestDto.getPublisher(),
                requestDto.getPrice(),
                requestDto.getStock(),
                requestDto.getDescription()
        );


        return bookRepository.save(book);
    }


    // 전체 조회
    public List<Book> findAll() {

        return bookRepository.findAll();
    }
}