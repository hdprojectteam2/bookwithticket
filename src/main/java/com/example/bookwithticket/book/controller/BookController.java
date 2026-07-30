package com.example.bookwithticket.book.controller;


import com.example.bookwithticket.book.dto.BookRequestDto;
import com.example.bookwithticket.book.dto.BookResponseDto;
import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.service.BookService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.bookwithticket.book.dto.BookImportRequestDto;
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
    // 도서 상세 조회
    @GetMapping("/{id}")
    public BookResponseDto findById(
            @PathVariable Long id
    ) {

        Book book = bookService.findById(id);

        return new BookResponseDto(book);
    }

    // 도서 수정
    @PutMapping("/{id}")
    public BookResponseDto update(
            @PathVariable Long id,
            @RequestBody BookRequestDto requestDto
    ) {

        Book book = bookService.update(id, requestDto);

        return new BookResponseDto(book);
    }

    // 도서 삭제
    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id
    ) {

        bookService.delete(id);

        return "도서 삭제 완료";
    }

    @PostMapping("/import")
    public BookResponseDto importBook(
            @RequestBody BookImportRequestDto requestDto
    ) {


        Book book =
                bookService.saveImport(requestDto);


        return new BookResponseDto(book);
    }


}