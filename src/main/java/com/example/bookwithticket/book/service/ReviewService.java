package com.example.bookwithticket.book.service;


import com.example.bookwithticket.book.dto.ReviewRequestDto;
import com.example.bookwithticket.book.entity.Book;
import com.example.bookwithticket.book.entity.Review;
import com.example.bookwithticket.book.repository.BookRepository;
import com.example.bookwithticket.book.repository.ReviewRepository;

import com.example.bookwithticket.member.entity.Member;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ReviewService {


    private final ReviewRepository reviewRepository;

    private final BookRepository bookRepository;


    public ReviewService(
            ReviewRepository reviewRepository,
            BookRepository bookRepository
    ) {

        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;

    }


    /* =====================================================
       리뷰 작성
    ===================================================== */

    public Review save(
            Long bookId,
            Member member,
            ReviewRequestDto requestDto
    ) {

        Book book =
                bookRepository.findById(bookId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "도서를 찾을 수 없습니다."
                                )
                        );


        if (
                requestDto.getRating() < 1 ||
                        requestDto.getRating() > 5
        ) {

            throw new RuntimeException(
                    "평점은 1점부터 5점까지 가능합니다."
            );

        }


        Review review =
                new Review(
                        member,
                        book,
                        requestDto.getContent(),
                        requestDto.getRating()
                );


        return reviewRepository.save(review);

    }


    /* =====================================================
       특정 도서 리뷰 조회
    ===================================================== */

    public List<Review> findAll(
            Long bookId
    ) {

        Book book =
                bookRepository.findById(bookId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "도서를 찾을 수 없습니다."
                                )
                        );


        return reviewRepository
                .findByBookOrderByCreatedAtDesc(
                        book
                );

    }


    /* =====================================================
       리뷰 수정
    ===================================================== */

    public Review update(
            Long reviewId,
            Member member,
            ReviewRequestDto requestDto
    ) {

        Review review =
                reviewRepository.findById(reviewId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "리뷰를 찾을 수 없습니다."
                                )
                        );


        // 본인이 작성한 리뷰인지 확인
        if (
                !review.getMember()
                        .getId()
                        .equals(member.getId())
        ) {

            throw new RuntimeException(
                    "수정 권한이 없습니다."
            );

        }


        // 평점 검사
        if (
                requestDto.getRating() < 1 ||
                        requestDto.getRating() > 5
        ) {

            throw new RuntimeException(
                    "평점은 1점부터 5점까지 가능합니다."
            );

        }


        // 리뷰 내용 / 평점 변경
        review.update(
                requestDto.getContent(),
                requestDto.getRating()
        );


        return reviewRepository.save(review);

    }


    /* =====================================================
       리뷰 삭제
    ===================================================== */

    public void delete(
            Long reviewId,
            Member member
    ) {

        Review review =
                reviewRepository.findById(reviewId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "리뷰를 찾을 수 없습니다."
                                )
                        );


        // 본인이 작성한 리뷰인지 확인
        if (
                !review.getMember()
                        .getId()
                        .equals(member.getId())
        ) {

            throw new RuntimeException(
                    "삭제 권한이 없습니다."
            );

        }


        reviewRepository.delete(review);

    }


    /* =====================================================
       평균 평점
    ===================================================== */

    public Double getAverageRating(
            Long bookId
    ) {

        Double avg =
                reviewRepository.findAverageRating(
                        bookId
                );


        if (avg == null) {

            return 0.0;

        }


        return Math.round(avg * 10) / 10.0;

    }


    /* =====================================================
       리뷰 개수
    ===================================================== */

    public long getReviewCount(
            Long bookId
    ) {

        return reviewRepository.countReview(
                bookId
        );

    }


    /* =====================================================
       내가 작성한 리뷰
    ===================================================== */

    public List<Review> findByMember(
            Member member
    ) {

        return reviewRepository.findByMember(
                member
        );

    }

}