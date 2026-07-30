package com.example.bookwithticket.book.service;

import com.example.bookwithticket.book.dto.BookImportRequestDto;
import com.example.bookwithticket.book.dto.BookRequestDto;
import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class BookService {


    private final BookRepository bookRepository;


    public BookService(
            BookRepository bookRepository
    ) {
        this.bookRepository = bookRepository;
    }



    // 도서 등록
    public Book save(BookRequestDto requestDto) {


        Book book = new Book(
                requestDto.getIsbn(),
                requestDto.getTitle(),
                requestDto.getAuthor(),
                requestDto.getPublisher(),
                requestDto.getPrice(),
                requestDto.getThumbnail(),
                requestDto.getDescription()
        );


        book.setStock(requestDto.getStock());


        return bookRepository.save(book);
    }



    // 전체 조회
    public List<Book> findAll() {

        return bookRepository.findAll();
    }



    // 단건 조회
    public Book findById(Long id) {

        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("도서를 찾을 수 없습니다.")
                );
    }



    // 수정
    public Book update(
            Long id,
            BookRequestDto requestDto
    ) {


        Book book = findById(id);


        book.setTitle(requestDto.getTitle());

        book.setAuthor(requestDto.getAuthor());

        book.setPublisher(requestDto.getPublisher());

        book.setPrice(requestDto.getPrice());

        book.setThumbnail(requestDto.getThumbnail());

        book.setDescription(requestDto.getDescription());

        book.setStock(requestDto.getStock());


        return bookRepository.save(book);
    }



    // 삭제
    public void delete(Long id) {

        Book book = findById(id);

        bookRepository.delete(book);
    }

    // 도서 검색 (임시)
    public List<Book> search(String keyword) {

        return bookRepository.findAll()
                .stream()
                .filter(book ->
                        book.getTitle().contains(keyword)
                )
                .toList();
    }

    public Book saveImport(BookImportRequestDto dto) {


        Book book = new Book(
                dto.getIsbn(),
                dto.getTitle(),
                dto.getAuthor(),
                dto.getPublisher(),
                dto.getPrice(),
                dto.getThumbnail(),
                dto.getDescription()
        );


        return bookRepository.save(book);
    }
}