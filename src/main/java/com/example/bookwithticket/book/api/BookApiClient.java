package com.example.bookwithticket.book.api;


import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class BookApiClient {


    private final AladinBookProperties properties;


    public BookApiClient(
            AladinBookProperties properties
    ) {
        this.properties = properties;
    }



    public String searchBook(String keyword) {


        RestTemplate restTemplate = new RestTemplate();


        String url =
                "https://dapi.kakao.com/v3/search/book?query="
                        + keyword;



        HttpHeaders headers = new HttpHeaders();

        headers.set(
                "Authorization",
                "KakaoAK " + properties.getApiKey()
        );


        HttpEntity<Void> entity =
                new HttpEntity<>(headers);



        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        String.class
                );


        return response.getBody();
    }
}