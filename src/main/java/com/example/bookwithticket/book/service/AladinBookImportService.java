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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class AladinBookImportService {


    private final AladinBookClient aladinBookClient;

    private final BookRepository bookRepository;


    public AladinBookImportService(
            AladinBookClient aladinBookClient,
            BookRepository bookRepository
    ) {

        this.aladinBookClient =
                aladinBookClient;

        this.bookRepository =
                bookRepository;
    }


    /* =====================================================
       기존 키워드 적재
    ===================================================== */

    @Transactional
    public List<BookResponseDto> importByKeyword(
            String keyword,
            int maxResults,
            int defaultStock
    ) {

        AladinBookResponse response =
                aladinBookClient.search(
                        keyword,
                        maxResults,
                        1
                );


        List<BookResponseDto> imported =
                new ArrayList<>();


        for (
                AladinBookResponse.Item item
                : response.getItem()
        ) {

            if (
                    item.getIsbn13() == null ||
                            item.getIsbn13().isBlank()
            ) {
                continue;
            }


            if (
                    bookRepository.existsByIsbn(
                            item.getIsbn13()
                    )
            ) {
                continue;
            }


            Book book =
                    createBookFromAladin(
                            item,
                            defaultStock
                    );


            imported.add(
                    new BookResponseDto(
                            bookRepository.save(
                                    book
                            )
                    )
            );
        }


        return imported;
    }


    /* =====================================================
       특정 카테고리 여러 페이지 적재
    ===================================================== */

    public ImportResult importByCategory(
            int categoryId,
            int pages,
            int pageSize,
            int defaultStock
    ) {

        validateImportOptions(
                pages,
                pageSize
        );


        int requested = 0;
        int inserted = 0;
        int skipped = 0;
        int failed = 0;


        for (
                int page = 1;
                page <= pages;
                page++
        ) {

            AladinBookResponse response;


            try {

                response =
                        aladinBookClient.list(
                                categoryId,
                                pageSize,
                                page
                        );

            } catch (Exception e) {

                failed++;

                System.err.println(
                        "[ALADIN] 카테고리 호출 실패"
                                + " categoryId="
                                + categoryId
                                + ", page="
                                + page
                                + ", error="
                                + e.getMessage()
                );

                continue;
            }


            List<AladinBookResponse.Item> items =
                    response.getItem();


            if (
                    items == null ||
                            items.isEmpty()
            ) {

                break;
            }


            for (
                    AladinBookResponse.Item item
                    : items
            ) {

                requested++;


                try {

                    String isbn =
                            item.getIsbn13();


                    if (
                            isbn == null ||
                                    isbn.isBlank()
                    ) {

                        skipped++;

                        continue;
                    }


                    /*
                     * 기존 도서는 중복 저장하지 않음
                     */
                    if (
                            bookRepository.existsByIsbn(
                                    isbn.trim()
                            )
                    ) {

                        skipped++;

                        continue;
                    }


                    Book book =
                            createBookFromAladin(
                                    item,
                                    defaultStock
                            );


                    bookRepository.saveAndFlush(
                            book
                    );


                    inserted++;


                } catch (Exception e) {

                    failed++;


                    System.err.println(
                            "[ALADIN] 도서 저장 실패: "
                                    + item.getTitle()
                                    + " / "
                                    + e.getMessage()
                    );

                }

            }


            /*
             * 요청한 개수보다 적게 오면
             * 마지막 페이지로 간주
             */
            if (
                    items.size() <
                            pageSize
            ) {

                break;
            }

        }


        return new ImportResult(
                requested,
                inserted,
                skipped,
                failed
        );
    }


    /* =====================================================
       여러 카테고리 한꺼번에 적재
    ===================================================== */


    public Map<String, ImportResult>
    importAllCategories(
            Map<String, Integer> categories,
            int pages,
            int pageSize,
            int defaultStock
    ) {

        validateImportOptions(
                pages,
                pageSize
        );


        Map<String, ImportResult> results =
                new LinkedHashMap<>();


        for (
                Map.Entry<String, Integer> entry
                : categories.entrySet()
        ) {

            String categoryName =
                    entry.getKey();


            Integer categoryId =
                    entry.getValue();


            if (
                    categoryId == null ||
                            categoryId <= 0
            ) {

                continue;
            }


            ImportResult result =
                    importByCategory(
                            categoryId,
                            pages,
                            pageSize,
                            defaultStock
                    );


            results.put(
                    categoryName,
                    result
            );

        }


        return results;
    }


    /* =====================================================
       알라딘 Item -> Book 변환
    ===================================================== */

    private Book createBookFromAladin(
            AladinBookResponse.Item item,
            int defaultStock
    ) {

        String category =
                BookCategory
                        .fromAladinCategoryName(
                                item.getCategoryName()
                        )
                        .getLabel();


        return Book.create(
                item.getIsbn13()
                        .trim(),

                item.getTitle(),

                item.getAuthor(),

                item.getPublisher(),

                item.getPriceStandard(),

                item.getPriceSales(),

                normalizeCoverUrl(
                        item.getCover()
                ),

                item.getDescription(),

                category,

                item.getCategoryName(),

                parsePublishedDate(
                        item.getPubDate()
                ),

                Math.max(
                        defaultStock,
                        0
                )
        );
    }


    /* =====================================================
       날짜
    ===================================================== */

    private LocalDate parsePublishedDate(
            String value
    ) {

        if (
                value == null ||
                        value.isBlank()
        ) {

            return null;
        }


        try {

            return LocalDate.parse(
                    value.trim()
            );

        } catch (
                DateTimeParseException e
        ) {

            return null;
        }

    }


    /* =====================================================
       표지 URL
    ===================================================== */

    private String normalizeCoverUrl(
            String cover
    ) {

        if (
                cover == null ||
                        cover.isBlank()
        ) {

            return null;
        }


        if (
                cover.startsWith("//")
        ) {

            return "https:" +
                    cover;

        }


        if (
                cover.startsWith(
                        "http://"
                )
        ) {

            return "https://" +
                    cover.substring(
                            "http://".length()
                    );

        }


        return cover;
    }


    /* =====================================================
       옵션 검사
    ===================================================== */

    private void validateImportOptions(
            int pages,
            int pageSize
    ) {

        if (
                pages < 1 ||
                        pages > 20
        ) {

            throw new IllegalArgumentException(
                    "pages는 1~20 사이여야 합니다."
            );

        }


        if (
                pageSize < 1 ||
                        pageSize > 50
        ) {

            throw new IllegalArgumentException(
                    "pageSize는 1~50 사이여야 합니다."
            );

        }

    }


    /* =====================================================
       결과 DTO
    ===================================================== */

    public record ImportResult(
            int requested,
            int inserted,
            int skipped,
            int failed
    ) {
    }

}