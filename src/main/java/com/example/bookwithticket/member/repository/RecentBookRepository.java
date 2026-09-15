package com.example.bookwithticket.member.repository;

import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.entity.RecentBook;
import com.example.bookwithticket.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecentBookRepository extends JpaRepository<RecentBook, Long> {
    List<RecentBook> findTop10ByMemberOrderByViewedAtDesc(Member member);
    Optional<RecentBook> findByMemberAndBook(Member member, Book book);
}
