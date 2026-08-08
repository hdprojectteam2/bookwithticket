package com.example.bookwithticket.member.service;


import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.repository.BookRepository;

import com.example.bookwithticket.member.entity.FavoriteBook;
import com.example.bookwithticket.member.entity.Member;

import com.example.bookwithticket.member.repository.FavoriteBookRepository;

import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class FavoriteBookService {


    private final FavoriteBookRepository favoriteBookRepository;

    private final BookRepository bookRepository;




    public FavoriteBookService(
            FavoriteBookRepository favoriteBookRepository,
            BookRepository bookRepository
    ){

        this.favoriteBookRepository =
                favoriteBookRepository;

        this.bookRepository =
                bookRepository;

    }







    // 관심 도서 추가

    public FavoriteBook addFavorite(
            Member member,
            Long bookId
    ){


        Book book =
                bookRepository.findById(bookId)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "도서를 찾을 수 없습니다."
                                )
                        );



        if(
                favoriteBookRepository
                        .findByMemberAndBook(member, book)
                        .isPresent()
        ){

            throw new RuntimeException(
                    "이미 관심 도서입니다."
            );

        }




        FavoriteBook favoriteBook =
                new FavoriteBook(
                        member,
                        book
                );



        return favoriteBookRepository.save(
                favoriteBook
        );

    }








    // 관심 도서 삭제

    public void removeFavorite(
            Member member,
            Long bookId
    ){


        Book book =
                bookRepository.findById(bookId)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "도서를 찾을 수 없습니다."
                                )
                        );



        FavoriteBook favoriteBook =

                favoriteBookRepository
                        .findByMemberAndBook(
                                member,
                                book
                        )

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "관심 도서가 아닙니다."
                                )
                        );



        favoriteBookRepository.delete(
                favoriteBook
        );

    }








    // 관심 도서 목록

    public List<FavoriteBook> findFavorites(
            Member member
    ){


        return favoriteBookRepository
                .findTop10ByMemberOrderByCreatedAtDesc(
                        member
                );

    }


}