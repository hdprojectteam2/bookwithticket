package com.example.bookwithticket.member.service;

import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.exception.BookNotFoundException;
import com.example.bookwithticket.book.repository.BookRepository;
import com.example.bookwithticket.member.entity.FavoriteBook;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.exception.DuplicateFavoriteException;
import com.example.bookwithticket.member.exception.FavoriteNotFoundException;
import com.example.bookwithticket.member.repository.FavoriteBookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FavoriteBookService {
    private final FavoriteBookRepository favoriteBookRepository;
    private final BookRepository bookRepository;

    public FavoriteBookService(FavoriteBookRepository favoriteBookRepository, BookRepository bookRepository) {
        this.favoriteBookRepository = favoriteBookRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public FavoriteBook addFavorite(Member member, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (favoriteBookRepository.existsByMemberAndBook(member, book)) {
            throw new DuplicateFavoriteException();
        }

        return favoriteBookRepository.save(new FavoriteBook(member, book));
    }

    @Transactional
    public void removeFavorite(Member member, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        FavoriteBook favorite = favoriteBookRepository.findByMemberAndBook(member, book)
                .orElseThrow(FavoriteNotFoundException::new);

        favoriteBookRepository.delete(favorite);
    }

    public List<FavoriteBook> findFavorites(Member member) {
        return favoriteBookRepository.findTop20ByMemberOrderByCreatedAtDesc(member);
    }
}
