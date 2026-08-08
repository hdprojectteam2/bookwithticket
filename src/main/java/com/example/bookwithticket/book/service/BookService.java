package com.example.bookwithticket.book.service;


import com.example.bookwithticket.book.dto.BookImportRequestDto;
import com.example.bookwithticket.book.dto.BookRequestDto;
import com.example.bookwithticket.book.dto.StockRequestDto;
import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.repository.BookRepository;

import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.RecentBookService;

import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class BookService {


    private final BookRepository bookRepository;

    private final RecentBookService recentBookService;



    public BookService(
            BookRepository bookRepository,
            RecentBookService recentBookService
    ){

        this.bookRepository = bookRepository;

        this.recentBookService = recentBookService;

    }







    // 도서 등록

    public Book save(
            BookRequestDto requestDto
    ){


        Book book = new Book(

                requestDto.getIsbn(),

                requestDto.getTitle(),

                requestDto.getAuthor(),

                requestDto.getPublisher(),

                requestDto.getPrice(),

                requestDto.getThumbnail(),

                requestDto.getDescription()

        );



        book.setCategory(
                requestDto.getCategory()
        );


        book.setStock(
                requestDto.getStock()
        );


        return bookRepository.save(book);

    }







    // 전체 조회

    public List<Book> findAll(){

        return bookRepository.findAll();

    }







    // 상세 조회 + 조회수 + 최근 본 도서

    public Book findById(
            Long id,
            Member member
    ){


        Book book =
                bookRepository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "도서를 찾을 수 없습니다."
                                )
                        );



        book.increaseViewCount();


        bookRepository.save(book);



        if(member != null){

            recentBookService.save(
                    member,
                    book
            );

        }



        return book;

    }







    // 수정

    public Book update(
            Long id,
            BookRequestDto requestDto
    ){


        Book book = findById(id, null);



        book.setTitle(
                requestDto.getTitle()
        );


        book.setAuthor(
                requestDto.getAuthor()
        );


        book.setPublisher(
                requestDto.getPublisher()
        );


        book.setPrice(
                requestDto.getPrice()
        );


        book.setThumbnail(
                requestDto.getThumbnail()
        );


        book.setDescription(
                requestDto.getDescription()
        );


        book.setCategory(
                requestDto.getCategory()
        );


        book.setStock(
                requestDto.getStock()
        );



        return bookRepository.save(book);

    }







    // 삭제

    public void delete(Long id){


        Book book =
                findById(id, null);


        bookRepository.delete(book);

    }







    // 검색

    public List<Book> search(
            String keyword
    ){

        return bookRepository
                .findByTitleContaining(keyword);

    }







    // 자동완성

    public List<String> autocomplete(
            String keyword
    ){

        return bookRepository
                .findTop5ByTitleContaining(keyword)

                .stream()

                .map(Book::getTitle)

                .distinct()

                .toList();

    }







    // 카테고리

    public List<Book> findByCategory(
            String category
    ){

        return bookRepository
                .findByCategory(category);

    }







    // 인기 도서

    public List<Book> findPopularBooks(){

        return bookRepository
                .findTop10ByOrderByViewCountDesc();

    }







    // 신간 도서

    public List<Book> findNewBooks(){

        return bookRepository
                .findTop10ByOrderByCreatedAtDesc();

    }







    // 알라딘 import

    public Book saveImport(
            BookImportRequestDto dto
    ){


        Book book = new Book(

                dto.getIsbn(),

                dto.getTitle(),

                dto.getAuthor(),

                dto.getPublisher(),

                dto.getPrice(),

                dto.getThumbnail(),

                dto.getDescription()

        );


        book.setStock(10);


        return bookRepository.save(book);

    }







    // 재고 설정

    public Book updateStock(
            Long id,
            StockRequestDto requestDto
    ){


        Book book =
                findById(id, null);


        book.setStock(
                requestDto.getStock()
        );


        return bookRepository.save(book);

    }







    // 재고 감소

    public Book decreaseStock(
            Long id,
            int quantity
    ){


        Book book =
                findById(id, null);


        book.decreaseStock(quantity);


        return bookRepository.save(book);

    }







    // 재고 증가

    public Book increaseStock(
            Long id,
            int quantity
    ){


        Book book =
                findById(id, null);


        book.increaseStock(quantity);


        return bookRepository.save(book);

    }


}