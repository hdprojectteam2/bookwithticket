package com.example.bookwithticket.member.service;

import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.entity.RecentBook;
import com.example.bookwithticket.member.repository.RecentBookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecentBookService {
    private final RecentBookRepository repository;

    public RecentBookService(RecentBookRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void save(Member member, Book book) {
        RecentBook recent = repository.findByMemberAndBook(member, book)
                .orElseGet(() -> new RecentBook(member, book));

        recent.touch();
        repository.save(recent);
    }

    public List<RecentBook> findAll(Member member) {
        return repository.findTop10ByMemberOrderByViewedAtDesc(member);
    }
}
