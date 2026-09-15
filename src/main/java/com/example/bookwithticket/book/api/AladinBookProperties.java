package com.example.bookwithticket.book.api;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class AladinBookProperties {


    @Value("${aladin.book.api-key}")
    private String apiKey;


    public String getApiKey() {
        return apiKey;
    }
}