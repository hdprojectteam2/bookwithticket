package com.example.bookwithticket.book.api;


import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;



@Component
public class AladinBookClient {


    private final AladinBookProperties properties;



    public AladinBookClient(
            AladinBookProperties properties
    ) {

        this.properties = properties;

    }



    public AladinBookResponse search(String keyword) {


        RestTemplate restTemplate = new RestTemplate();


        String url =
                "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx"
                        + "?ttbkey=" + properties.getApiKey()
                        + "&Query=" + keyword
                        + "&QueryType=Keyword"
                        + "&MaxResults=10"
                        + "&start=1"
                        + "&SearchTarget=Book"
                        + "&output=js"
                        + "&Version=20131101";


        System.out.println("요청 URL : " + url);



        ResponseEntity<AladinBookResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        AladinBookResponse.class
                );

        System.out.println(
                "검색 개수 : "
                        + response.getBody().getItem().size()
        );


        response.getBody()
                .getItem()
                .forEach(book -> {
                    System.out.println(book.getTitle());
                });


        return response.getBody();

    }
}