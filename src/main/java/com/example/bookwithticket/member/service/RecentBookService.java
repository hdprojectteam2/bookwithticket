package com.example.bookwithticket.member.service;


import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.entity.RecentBook;
import com.example.bookwithticket.member.repository.RecentBookRepository;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RecentBookService {


    private final RecentBookRepository repository;



    public RecentBookService(
            RecentBookRepository repository
    ){

        this.repository = repository;

    }





    public void save(
            Member member,
            Book book
    ){

        RecentBook recentBook =
                new RecentBook(
                        member,
                        book
                );


        repository.save(recentBook);

    }





    public List<RecentBook> findAll(
            Member member
    ){

        return repository
                .findTop10ByMemberOrderByViewedAtDesc(
                        member
                );

    }


}