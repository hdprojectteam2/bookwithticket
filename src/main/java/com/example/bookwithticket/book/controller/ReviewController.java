package com.example.bookwithticket.book.controller;


import com.example.bookwithticket.book.dto.ReviewRequestDto;
import com.example.bookwithticket.book.dto.ReviewResponseDto;
import com.example.bookwithticket.book.entity.Review;
import com.example.bookwithticket.book.service.ReviewService;

import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.MemberService;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;


@RestController
@RequestMapping
public class ReviewController {



    private final ReviewService reviewService;

    private final MemberService memberService;





    public ReviewController(
            ReviewService reviewService,
            MemberService memberService
    ){

        this.reviewService = reviewService;

        this.memberService = memberService;

    }









    // 리뷰 작성

    @PostMapping("/books/{bookId}/reviews")
    public ReviewResponseDto save(

            @PathVariable Long bookId,

            @RequestBody ReviewRequestDto requestDto,

            Authentication authentication

    ){


        if(authentication == null){

            throw new RuntimeException(
                    "로그인이 필요합니다."
            );

        }


        Member member =
                memberService.findMyInfo(
                        authentication.getName()
                );


        Review review =
                reviewService.save(
                        bookId,
                        member,
                        requestDto
                );


        return new ReviewResponseDto(review);

    }



    // 리뷰 수정
    @PutMapping("/reviews/{reviewId}")
    public ReviewResponseDto update(

            @PathVariable Long reviewId,

            @RequestBody ReviewRequestDto requestDto,

            Authentication authentication

    ) {

        if (authentication == null) {

            throw new RuntimeException(
                    "로그인이 필요합니다."
            );

        }


        Member member =
                memberService.findMyInfo(
                        authentication.getName()
                );


        Review review =
                reviewService.update(
                        reviewId,
                        member,
                        requestDto
                );


        return new ReviewResponseDto(review);
    }






    // 리뷰 조회

    @GetMapping("/books/{bookId}/reviews")
    public List<ReviewResponseDto> findAll(

            @PathVariable Long bookId

    ){



        return reviewService.findAll(bookId)

                .stream()

                .map(ReviewResponseDto::new)

                .toList();

    }









    // 리뷰 삭제

    @DeleteMapping("/reviews/{reviewId}")
    public String delete(


            @PathVariable Long reviewId,


            Authentication authentication


    ){



        Member member =

                memberService.findMyInfo(
                        authentication.getName()
                );




        reviewService.delete(
                reviewId,
                member
        );



        return "리뷰 삭제 완료";

    }

    @GetMapping("/books/{bookId}/reviews/info")
    public Map<String,Object> reviewInfo(
            @PathVariable Long bookId
    ){


        Double averageRating =
                reviewService.getAverageRating(bookId);



        long reviewCount =
                reviewService.getReviewCount(bookId);



        return Map.of(
                "averageRating",
                averageRating,

                "reviewCount",
                reviewCount
        );


    }

    @GetMapping("/members/me/reviews")
    public List<ReviewResponseDto> myReviews(
            Authentication authentication
    ){
        if(authentication == null){

            throw new RuntimeException(
                    "로그인이 필요합니다."
            );

        }

        Member member =
                memberService.findMyInfo(
                        authentication.getName()
                );



        return reviewService.findByMember(member)

                .stream()

                .map(ReviewResponseDto::new)

                .toList();

    }


}