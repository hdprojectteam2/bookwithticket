package com.example.bookwithticket.book.controller;


import com.example.bookwithticket.book.dto.BookRequestDto;
import com.example.bookwithticket.book.dto.BookResponseDto;
import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/books")
public class BookController {


    private final BookService bookService;


    public BookController(BookService bookService) {
        this.bookService = bookService;
    }


    // 도서 등록
    @PostMapping
    public BookResponseDto save(
            @RequestBody BookRequestDto requestDto
    ) {

        Book book = bookService.save(requestDto);

        return new BookResponseDto(book);
    }



    // 도서 전체 조회
    @GetMapping
    public List<BookResponseDto> findAll() {

        return bookService.findAll()
                .stream()
                .map(BookResponseDto::new)
                .toList();
    }
}