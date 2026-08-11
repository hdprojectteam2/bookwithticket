package com.example.bookwithticket.book.service;

import com.example.bookwithticket.book.api.AladinBookClient;
import com.example.bookwithticket.book.api.AladinBookResponse;
import com.example.bookwithticket.book.dto.BookResponseDto;
import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.entity.BookCategory;
import com.example.bookwithticket.book.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AladinBookImportService {

    private final AladinBookClient aladinBookClient;
    private final BookRepository bookRepository;

    public AladinBookImportService(
            AladinBookClient aladinBookClient,
            BookRepository bookRepository
    ) {
        this.aladinBookClient = aladinBookClient;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public List<BookResponseDto> importByKeyword(
            String keyword,
            int maxResults,
            int defaultStock
    ) {
        AladinBookResponse response =
                aladinBookClient.search(keyword, maxResults, 1);

        List<BookResponseDto> imported = new ArrayList<>();

        for (AladinBookResponse.Item item : response.getItem()) {
            if (item.getIsbn13() == null || item.getIsbn13().isBlank()) {
                continue;
            }

            if (bookRepository.existsByIsbn(item.getIsbn13())) {
                continue;
            }

            Book book = Book.create(
                    item.getIsbn13(),
                    item.getTitle(),
                    item.getAuthor(),
                    item.getPublisher(),
                    item.getPriceStandard(),
                    item.getPriceSales(),
                    normalizeCoverUrl(item.getCover()),
                    item.getDescription(),
                    BookCategory.fromAladinCategoryName(item.getCategoryName()).getLabel(),
                    item.getCategoryName(),
                    parsePublishedDate(item.getPubDate()),
                    Math.max(defaultStock, 0)
            );

            imported.add(new BookResponseDto(bookRepository.save(book)));
        }

        return imported;
    }

    private LocalDate parsePublishedDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String normalizeCoverUrl(String cover) {
        if (cover == null || cover.isBlank()) {
            return null;
        }

        if (cover.startsWith("//")) {
            return "https:" + cover;
        }

        if (cover.startsWith("http://")) {
            return "https://" + cover.substring("http://".length());
        }

        return cover;
    }
}
