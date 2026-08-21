package com.example.bookwithticket.book.service;

import com.example.bookwithticket.book.dto.BookImportRequestDto;
import com.example.bookwithticket.book.dto.BookRequestDto;
import com.example.bookwithticket.book.dto.BookResponseDto;
import com.example.bookwithticket.book.dto.StockRequestDto;
import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.entity.BookCategory;
import com.example.bookwithticket.book.exception.BookNotFoundException;
import com.example.bookwithticket.book.exception.DuplicateBookException;
import com.example.bookwithticket.book.repository.BookRepository;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.RecentBookService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final RecentBookService recentBookService;

    public BookService(
            BookRepository bookRepository,
            RecentBookService recentBookService
    ) {
        this.bookRepository = bookRepository;
        this.recentBookService = recentBookService;
    }

    @Transactional
    public BookResponseDto save(BookRequestDto requestDto) {
        if (bookRepository.existsByIsbn(requestDto.getIsbn())) {
            throw new DuplicateBookException(requestDto.getIsbn());
        }

        Book book = Book.create(
                requestDto.getIsbn(),
                requestDto.getTitle(),
                requestDto.getAuthor(),
                requestDto.getPublisher(),
                requestDto.getPrice(),
                requestDto.getSalePrice(),
                requestDto.getThumbnail(),
                requestDto.getDescription(),
                normalizeCategory(requestDto.getCategory()),
                requestDto.getOriginalCategoryName(),
                requestDto.getPublishedDate(),
                requestDto.getStock()
        );

        return new BookResponseDto(bookRepository.save(book));
    }

    public Page<BookResponseDto> findBooks(
            String keyword,
            String category,
            String sort,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        String categoryType = normalizeCategoryNullable(category);
        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                resolveSort(sort)
        );

        return bookRepository
                .search(normalizeKeyword(keyword), categoryType, pageable)
                .map(BookResponseDto::new);
    }

    @Transactional
    public BookResponseDto findDetail(Long id, Member member) {
        Book book = getBook(id);

        book.increaseViewCount();

        if (member != null) {
            recentBookService.save(member, book);
        }

        return new BookResponseDto(book);
    }

    public Book findEntity(Long id) {
        return getBook(id);
    }

    @Transactional
    public BookResponseDto update(Long id, BookRequestDto requestDto) {
        Book book = getBook(id);

        book.update(
                requestDto.getTitle(),
                requestDto.getAuthor(),
                requestDto.getPublisher(),
                requestDto.getPrice(),
                requestDto.getSalePrice(),
                requestDto.getThumbnail(),
                requestDto.getDescription(),
                normalizeCategory(requestDto.getCategory()),
                requestDto.getOriginalCategoryName(),
                requestDto.getPublishedDate(),
                requestDto.getStock()
        );

        return new BookResponseDto(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = getBook(id);
        bookRepository.delete(book);
    }

    public List<String> autocomplete(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        return bookRepository
                .findTop5ByTitleContainingIgnoreCaseOrderByViewCountDesc(keyword.trim())
                .stream()
                .map(Book::getTitle)
                .distinct()
                .toList();
    }

    public List<BookResponseDto> findPopularBooks() {
        return bookRepository
                .findTop10ByOrderByViewCountDescIdDesc()
                .stream()
                .map(BookResponseDto::new)
                .toList();
    }

    public List<BookResponseDto> findBestSellers() {
        return bookRepository
                .findTop10ByOrderBySalesCountDescIdDesc()
                .stream()
                .map(BookResponseDto::new)
                .toList();
    }

    public List<BookResponseDto> findNewBooks() {
        return bookRepository
                .findTop10ByOrderByPublishedDateDescIdDesc()
                .stream()
                .map(BookResponseDto::new)
                .toList();
    }

    public List<BookCategory> findCategories() {
        return Arrays.asList(BookCategory.values());
    }

    @Transactional
    public BookResponseDto updateStock(Long id, StockRequestDto dto) {
        Book book = getBook(id);

        book.changeStock(dto.getStock());

        return new BookResponseDto(book);
    }


    @Transactional
    public Book decreaseStock(Long id, int quantity) {
        Book book = getBook(id);
        book.decreaseStock(quantity);
        return book;
    }

    @Transactional
    public Book increaseStock(Long id, int quantity) {
        Book book = getBook(id);
        book.increaseStock(quantity);
        return book;
    }

    /**
     * 이전 수동 import API를 다른 코드가 참조하고 있을 때를 위한 호환 메서드.
     * 신규 알라딘 적재는 AladinBookImportService를 사용한다.
     */
    @Transactional
    public Book saveImport(BookImportRequestDto dto) {
        if (bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new DuplicateBookException(dto.getIsbn());
        }

        Book book = Book.create(
                dto.getIsbn(),
                dto.getTitle(),
                dto.getAuthor(),
                dto.getPublisher(),
                dto.getPrice(),
                dto.getPrice(),
                dto.getThumbnail(),
                dto.getDescription(),
                "기타",
                null,
                null,
                10
        );

        return bookRepository.save(book);
    }

    private Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private String normalizeCategory(String category) {
        return BookCategory.from(category).getLabel();
    }

    private String normalizeCategoryNullable(String category) {
        if (category == null || category.isBlank()) return null;
        return normalizeCategory(category);
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank() || sort.equalsIgnoreCase("latest")) {
            return Sort.by(
                    Sort.Order.desc("publishedDate"),
                    Sort.Order.desc("id")
            );
        }

        return switch (sort.toLowerCase()) {
            case "popular" -> Sort.by(
                    Sort.Order.desc("viewCount"),
                    Sort.Order.desc("id")
            );
            case "bestseller", "sales" -> Sort.by(
                    Sort.Order.desc("salesCount"),
                    Sort.Order.desc("id")
            );
            case "priceasc" -> Sort.by(
                    Sort.Order.asc("salePrice"),
                    Sort.Order.desc("id")
            );
            case "pricedesc" -> Sort.by(
                    Sort.Order.desc("salePrice"),
                    Sort.Order.desc("id")
            );
            case "oldest" -> Sort.by(
                    Sort.Order.asc("publishedDate"),
                    Sort.Order.asc("id")
            );
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 정렬입니다: " + sort
                            + " (latest, popular, bestseller, priceAsc, priceDesc, oldest)"
            );
        };
    }

    /*
     * 기존 코드 호환 메서드.
     * 새 Controller에서는 위 DTO 기반 메서드를 사용한다.
     */
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Book findById(Long id, Member member) {
        Book book = getBook(id);
        book.increaseViewCount();

        if (member != null) {
            recentBookService.save(member, book);
        }

        return book;
    }

    public List<Book> search(String keyword) {
        return bookRepository.findByTitleContaining(keyword);
    }

    public List<Book> findByCategory(String category) {
        return bookRepository.findByCategory(
                BookCategory.from(category).getLabel()
        );
    }
}
