package com.example.bookwithticket.book.controller;


import com.example.bookwithticket.book.dto.BookImportRequestDto;
import com.example.bookwithticket.book.dto.BookRequestDto;
import com.example.bookwithticket.book.dto.BookResponseDto;
import com.example.bookwithticket.book.dto.StockRequestDto;

import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.service.BookService;

import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;


import java.util.List;



@RestController
@RequestMapping("/books")
public class BookController {



    private final BookService bookService;

    private final MemberService memberService;




    public BookController(
            BookService bookService,
            MemberService memberService
    ){

        this.bookService = bookService;

        this.memberService = memberService;

    }








    // 등록

    @PostMapping
    public BookResponseDto save(
            @RequestBody BookRequestDto requestDto
    ){


        return new BookResponseDto(
                bookService.save(requestDto)
        );

    }







    // 전체 조회

    @GetMapping
    public List<BookResponseDto> findAll(){


        return bookService.findAll()

                .stream()

                .map(BookResponseDto::new)

                .toList();

    }







    // 상세 조회 + 최근 본 도서

    @GetMapping("/{id}")
    public BookResponseDto findById(
            @PathVariable Long id,
            Authentication authentication
    ){


        Member member = null;



        if(authentication != null){

            member =
                    memberService.findMyInfo(
                            authentication.getName()
                    );

        }



        Book book =
                bookService.findById(
                        id,
                        member
                );



        return new BookResponseDto(book);

    }







    // 수정

    @PutMapping("/{id}")
    public BookResponseDto update(
            @PathVariable Long id,
            @RequestBody BookRequestDto requestDto
    ){


        return new BookResponseDto(
                bookService.update(id, requestDto)
        );

    }







    // 삭제

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id
    ){

        bookService.delete(id);

        return "도서 삭제 완료";

    }







    // import

    @PostMapping("/import")
    public BookResponseDto importBook(
            @RequestBody BookImportRequestDto dto
    ){


        return new BookResponseDto(
                bookService.saveImport(dto)
        );

    }







    // 검색

    @GetMapping("/search")
    public List<BookResponseDto> search(
            @RequestParam String keyword
    ){

        return bookService.search(keyword)

                .stream()

                .map(BookResponseDto::new)

                .toList();

    }







    // 자동완성

    @GetMapping("/autocomplete")
    public List<String> autocomplete(
            @RequestParam String keyword
    ){

        return bookService.autocomplete(keyword);

    }







    // 카테고리

    @GetMapping("/category/{category}")
    public List<BookResponseDto> category(
            @PathVariable String category
    ){

        return bookService.findByCategory(category)

                .stream()

                .map(BookResponseDto::new)

                .toList();

    }







    // 인기 도서

    @GetMapping("/popular")
    public List<BookResponseDto> popular(){


        return bookService.findPopularBooks()

                .stream()

                .map(BookResponseDto::new)

                .toList();

    }







    // 신간

    @GetMapping("/new")
    public List<BookResponseDto> newBooks(){


        return bookService.findNewBooks()

                .stream()

                .map(BookResponseDto::new)

                .toList();

    }







    // 재고 수정

    @PutMapping("/{id}/stock")
    public BookResponseDto updateStock(
            @PathVariable Long id,
            @RequestBody StockRequestDto dto
    ){


        return new BookResponseDto(
                bookService.updateStock(id, dto)
        );

    }


}