package com.example.bookwithticket.book.api;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class AladinBookClient {

    private static final String SEARCH_URL =
            "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx";

    private final AladinBookProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    public AladinBookClient(AladinBookProperties properties) {
        this.properties = properties;
    }

    public AladinBookResponse search(String keyword, int maxResults, int start) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("검색어는 필수입니다.");
        }

        int safeMaxResults = Math.min(Math.max(maxResults, 1), 50);
        int safeStart = Math.max(start, 1);

        URI uri = UriComponentsBuilder
                .fromHttpUrl(SEARCH_URL)
                .queryParam("ttbkey", properties.getApiKey())
                .queryParam("Query", keyword.trim())
                .queryParam("QueryType", "Keyword")
                .queryParam("MaxResults", safeMaxResults)
                .queryParam("start", safeStart)
                .queryParam("SearchTarget", "Book")
                .queryParam("output", "js")
                .queryParam("Version", "20131101")
                .queryParam("Cover", "Big")
                .build()
                .encode()
                .toUri();

        AladinBookResponse response =
                restTemplate.getForObject(uri, AladinBookResponse.class);

        if (response == null) {
            throw new IllegalStateException("알라딘 도서 API 응답이 비어 있습니다.");
        }

        return response;
    }

    // 기존 호출부 호환용
    public AladinBookResponse search(String keyword) {
        return search(keyword, 20, 1);
    }
}
