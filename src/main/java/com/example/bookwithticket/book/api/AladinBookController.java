package com.example.bookwithticket.book.api;


import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/aladin")
public class AladinBookController {


    private final AladinBookClient aladinBookClient;


    public AladinBookController(
            AladinBookClient aladinBookClient
    ) {

        this.aladinBookClient = aladinBookClient;

    }



    @GetMapping("/search")
    public AladinBookResponse search(
            @RequestParam String keyword
    ) {

        System.out.println("받은 검색어 = " + keyword);

        return aladinBookClient.search(keyword);

    }

}