package com.example.bookwithticket.book.controller;


import com.example.bookwithticket.book.dto.BookImportRequestDto;
import com.example.bookwithticket.book.dto.BookRequestDto;
import com.example.bookwithticket.book.dto.BookResponseDto;
import com.example.bookwithticket.book.dto.StockRequestDto;
import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/books")
public class BookController {


    private final BookService bookService;


    public BookController(
            BookService bookService
    ) {

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



    // 전체 조회
    @GetMapping
    public List<BookResponseDto> findAll() {


        return bookService.findAll()
                .stream()
                .map(BookResponseDto::new)
                .toList();

    }



    // 상세 조회
    @GetMapping("/{id}")
    public BookResponseDto findById(
            @PathVariable Long id
    ) {


        Book book = bookService.findById(id);


        return new BookResponseDto(book);

    }



    // 수정
    @PutMapping("/{id}")
    public BookResponseDto update(
            @PathVariable Long id,
            @RequestBody BookRequestDto requestDto
    ) {


        Book book = bookService.update(id, requestDto);


        return new BookResponseDto(book);

    }



    // 삭제
    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id
    ) {


        bookService.delete(id);


        return "도서 삭제 완료";

    }



    // 알라딘 import 저장
    @PostMapping("/import")
    public BookResponseDto importBook(
            @RequestBody BookImportRequestDto requestDto
    ) {


        Book book =
                bookService.saveImport(requestDto);


        return new BookResponseDto(book);

    }



    // 재고 설정
    @PutMapping("/{id}/stock")
    public BookResponseDto updateStock(
            @PathVariable Long id,
            @RequestBody StockRequestDto requestDto
    ) {


        Book book =
                bookService.updateStock(id, requestDto);


        return new BookResponseDto(book);

    }



    // 주문 시 재고 감소
    @PutMapping("/{id}/stock/decrease")
    public BookResponseDto decreaseStock(
            @PathVariable Long id,
            @RequestParam int quantity
    ) {


        Book book =
                bookService.decreaseStock(id, quantity);


        return new BookResponseDto(book);

    }



    // 주문 취소 시 재고 증가
    @PutMapping("/{id}/stock/increase")
    public BookResponseDto increaseStock(
            @PathVariable Long id,
            @RequestParam int quantity
    ) {


        Book book =
                bookService.increaseStock(id, quantity);


        return new BookResponseDto(book);

    }

}