package com.example.bookwithticket.book.api;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class AladinBookClient {

    private static final String SEARCH_URL =
            "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx";

    private static final String LIST_URL =
            "https://www.aladin.co.kr/ttb/api/ItemList.aspx";

    private final AladinBookProperties properties;

    private final RestTemplate restTemplate =
            new RestTemplate();


    public AladinBookClient(
            AladinBookProperties properties
    ) {
        this.properties = properties;
    }


    /* =====================================================
       기존 키워드 검색
    ===================================================== */

    public AladinBookResponse search(
            String keyword,
            int maxResults,
            int start
    ) {

        if (
                keyword == null ||
                        keyword.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "검색어는 필수입니다."
            );
        }


        int safeMaxResults =
                Math.min(
                        Math.max(maxResults, 1),
                        50
                );


        int safeStart =
                Math.max(start, 1);


        URI uri =
                UriComponentsBuilder
                        .fromHttpUrl(
                                SEARCH_URL
                        )
                        .queryParam(
                                "ttbkey",
                                properties.getApiKey()
                        )
                        .queryParam(
                                "Query",
                                keyword.trim()
                        )
                        .queryParam(
                                "QueryType",
                                "Keyword"
                        )
                        .queryParam(
                                "MaxResults",
                                safeMaxResults
                        )
                        .queryParam(
                                "start",
                                safeStart
                        )
                        .queryParam(
                                "SearchTarget",
                                "Book"
                        )
                        .queryParam(
                                "output",
                                "js"
                        )
                        .queryParam(
                                "Version",
                                "20131101"
                        )
                        .queryParam(
                                "Cover",
                                "Big"
                        )
                        .build()
                        .encode()
                        .toUri();


        AladinBookResponse response =
                restTemplate.getForObject(
                        uri,
                        AladinBookResponse.class
                );


        if (response == null) {

            throw new IllegalStateException(
                    "알라딘 도서 API 응답이 비어 있습니다."
            );

        }


        return response;
    }


    public AladinBookResponse search(
            String keyword
    ) {

        return search(
                keyword,
                20,
                1
        );
    }


    /* =====================================================
       카테고리별 리스트 조회
    ===================================================== */

    public AladinBookResponse list(
            int categoryId,
            int maxResults,
            int start
    ) {

        if (categoryId <= 0) {

            throw new IllegalArgumentException(
                    "알라딘 CategoryId는 1 이상이어야 합니다."
            );

        }


        int safeMaxResults =
                Math.min(
                        Math.max(maxResults, 1),
                        50
                );


        int safeStart =
                Math.max(start, 1);


        URI uri =
                UriComponentsBuilder
                        .fromHttpUrl(
                                LIST_URL
                        )
                        .queryParam(
                                "ttbkey",
                                properties.getApiKey()
                        )
                        .queryParam(
                                "QueryType",
                                "ItemNewAll"
                        )
                        .queryParam(
                                "MaxResults",
                                safeMaxResults
                        )
                        .queryParam(
                                "start",
                                safeStart
                        )
                        .queryParam(
                                "SearchTarget",
                                "Book"
                        )
                        .queryParam(
                                "CategoryId",
                                categoryId
                        )
                        .queryParam(
                                "output",
                                "js"
                        )
                        .queryParam(
                                "Version",
                                "20131101"
                        )
                        .queryParam(
                                "Cover",
                                "Big"
                        )
                        .build()
                        .encode()
                        .toUri();


        AladinBookResponse response =
                restTemplate.getForObject(
                        uri,
                        AladinBookResponse.class
                );


        if (response == null) {

            throw new IllegalStateException(
                    "알라딘 카테고리 API 응답이 비어 있습니다."
            );

        }


        return response;
    }

}