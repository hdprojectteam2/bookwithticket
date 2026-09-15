package com.example.bookwithticket.book.api;


import com.example.bookwithticket.book.dto.BookResponseDto;
import com.example.bookwithticket.book.service.AladinBookImportService;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/aladin")
public class AladinBookController {


    private final AladinBookClient aladinBookClient;

    private final AladinBookImportService
            aladinBookImportService;


    public AladinBookController(
            AladinBookClient aladinBookClient,
            AladinBookImportService
                    aladinBookImportService
    ) {

        this.aladinBookClient =
                aladinBookClient;

        this.aladinBookImportService =
                aladinBookImportService;
    }


    /* =====================================================
       기존 알라딘 검색
    ===================================================== */

    @GetMapping("/search")
    public AladinBookResponse search(
            @RequestParam String keyword
    ) {

        return aladinBookClient.search(
                keyword
        );

    }


    /* =====================================================
       기존 키워드 DB 적재
    ===================================================== */

    @PostMapping("/import/keyword")
    public List<BookResponseDto>
    importKeyword(

            @RequestParam
            String keyword,

            @RequestParam(
                    defaultValue = "20"
            )
            int maxResults,

            @RequestParam(
                    defaultValue = "100"
            )
            int stock
    ) {

        return aladinBookImportService
                .importByKeyword(
                        keyword,
                        maxResults,
                        stock
                );

    }


    /* =====================================================
       특정 알라딘 CategoryId DB 적재
    ===================================================== */

    @PostMapping("/import/category")
    public AladinBookImportService.ImportResult
    importCategory(

            @RequestParam
            int categoryId,

            @RequestParam(
                    defaultValue = "5"
            )
            int pages,

            @RequestParam(
                    defaultValue = "50"
            )
            int pageSize,

            @RequestParam(
                    defaultValue = "100"
            )
            int stock
    ) {

        return aladinBookImportService
                .importByCategory(
                        categoryId,
                        pages,
                        pageSize,
                        stock
                );

    }


    /* =====================================================
       Booketing 주요 카테고리 전체 적재

       아래 ID들은 반드시 실제 알라딘 CategoryId로
       바꿔서 사용해야 함.
    ===================================================== */

    @PostMapping("/import/all")
    public Map<String,
            AladinBookImportService.ImportResult>
    importAll(

            @RequestParam(
                    defaultValue = "5"
            )
            int pages,

            @RequestParam(
                    defaultValue = "50"
            )
            int pageSize,

            @RequestParam(
                    defaultValue = "100"
            )
            int stock
    ) {

        Map<String, Integer> categories =
                new LinkedHashMap<>();

        categories.put("소설", 1);
        categories.put("시/에세이", 55889);
        categories.put("인문", 656);
        categories.put("경제/경영", 170);
        categories.put("자기계발", 336);
        categories.put("정치/사회", 798);
        categories.put("역사/문화", 74);
        categories.put("과학", 987);
        categories.put("컴퓨터/IT", 351);
        categories.put("여행", 1196);
        categories.put("어린이", 1108);
        categories.put("만화", 2551);
        categories.put("외국어", 1322);
        categories.put("수험서", 1383);


        return aladinBookImportService
                .importAllCategories(
                        categories,
                        pages,
                        pageSize,
                        stock
                );

    }

}