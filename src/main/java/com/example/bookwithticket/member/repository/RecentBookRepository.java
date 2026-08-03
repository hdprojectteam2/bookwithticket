package com.example.bookwithticket.member.repository;


import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.entity.RecentBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface RecentBookRepository
        extends JpaRepository<RecentBook, Long> {


    List<RecentBook>
    findTop10ByMemberOrderByViewedAtDesc(
            Member member
    );


}