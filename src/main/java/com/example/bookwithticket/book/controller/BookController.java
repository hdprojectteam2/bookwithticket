package com.example.bookwithticket.book.controller;

import com.example.bookwithticket.book.dto.BookCategoryResponseDto;
import com.example.bookwithticket.book.dto.BookRequestDto;
import com.example.bookwithticket.book.dto.BookResponseDto;
import com.example.bookwithticket.book.dto.StockRequestDto;
import com.example.bookwithticket.book.service.AladinBookImportService;
import com.example.bookwithticket.book.service.BookService;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final AladinBookImportService aladinBookImportService;
    private final MemberService memberService;

    public BookController(
            BookService bookService,
            AladinBookImportService aladinBookImportService,
            MemberService memberService
    ) {
        this.bookService = bookService;
        this.aladinBookImportService = aladinBookImportService;
        this.memberService = memberService;
    }

    /**
     * 서점형 통합 목록 API.
     *
     * 예:
     * GET /books?page=0&size=20
     * GET /books?keyword=스프링
     * GET /books?category=IT
     * GET /books?category=소설&sort=bestseller
     * GET /books?sort=priceAsc
     */
    @GetMapping
    public Page<BookResponseDto> findBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return bookService.findBooks(
                keyword,
                category,
                sort,
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public BookResponseDto findDetail(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Member member = null;

        if (authentication != null && authentication.isAuthenticated()) {
            member = memberService.findMyInfo(authentication.getName());
        }

        return bookService.findDetail(id, member);
    }

    @GetMapping("/categories")
    public List<BookCategoryResponseDto> categories() {
        return bookService.findCategories()
                .stream()
                .map(BookCategoryResponseDto::new)
                .toList();
    }

    @GetMapping("/popular")
    public List<BookResponseDto> popular() {
        return bookService.findPopularBooks();
    }

    @GetMapping("/bestsellers")
    public List<BookResponseDto> bestSellers() {
        return bookService.findBestSellers();
    }

    @GetMapping("/new")
    public List<BookResponseDto> newBooks() {
        return bookService.findNewBooks();
    }

    @GetMapping("/autocomplete")
    public List<String> autocomplete(
            @RequestParam String keyword
    ) {
        return bookService.autocomplete(keyword);
    }

    /**
     * 이전 프론트/테스트 호환용.
     * 실제 신규 프론트에서는 GET /books?keyword=... 를 사용한다.
     */
    @GetMapping("/search")
    public Page<BookResponseDto> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return bookService.findBooks(keyword, null, sort, page, size);
    }

    @PostMapping
    public ResponseEntity<BookResponseDto> save(
            @Valid @RequestBody BookRequestDto requestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookService.save(requestDto));
    }

    @PutMapping("/{id}")
    public BookResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDto requestDto
    ) {
        return bookService.update(id, requestDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/stock")
    public BookResponseDto updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockRequestDto dto
    ) {
        return bookService.updateStock(id, dto);
    }

    /**
     * 관리자용 알라딘 검색 → 우리 DB 적재.
     * 중복 ISBN은 자동으로 건너뛴다.
     */
    @PostMapping("/admin/import/aladin")
    public List<BookResponseDto> importFromAladin(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "20") int maxResults,
            @RequestParam(defaultValue = "10") int stock
    ) {
        return aladinBookImportService.importByKeyword(
                keyword,
                maxResults,
                stock
        );
    }
}
