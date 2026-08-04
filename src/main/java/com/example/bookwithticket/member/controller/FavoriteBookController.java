package com.example.bookwithticket.member.controller;


import com.example.bookwithticket.member.dto.FavoriteBookResponseDto;

import com.example.bookwithticket.member.entity.Member;

import com.example.bookwithticket.member.service.FavoriteBookService;
import com.example.bookwithticket.member.service.MemberService;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;


import java.util.List;



@RestController
@RequestMapping("/members/favorites")
public class FavoriteBookController {



    private final FavoriteBookService favoriteBookService;

    private final MemberService memberService;




    public FavoriteBookController(
            FavoriteBookService favoriteBookService,
            MemberService memberService
    ){

        this.favoriteBookService =
                favoriteBookService;

        this.memberService =
                memberService;

    }







    // 관심 도서 추가

    @PostMapping("/{bookId}")
    public FavoriteBookResponseDto add(
            @PathVariable Long bookId,
            Authentication authentication
    ){


        Member member =
                memberService.findMyInfo(
                        authentication.getName()
                );



        return new FavoriteBookResponseDto(

                favoriteBookService.addFavorite(
                        member,
                        bookId
                )

        );

    }








    // 관심 도서 삭제

    @DeleteMapping("/{bookId}")
    public String delete(
            @PathVariable Long bookId,
            Authentication authentication
    ){


        Member member =
                memberService.findMyInfo(
                        authentication.getName()
                );



        favoriteBookService.removeFavorite(
                member,
                bookId
        );



        return "관심 도서 삭제 완료";

    }








    // 관심 도서 조회

    @GetMapping
    public List<FavoriteBookResponseDto> findAll(
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



        return favoriteBookService
                .findFavorites(member)

                .stream()

                .map(FavoriteBookResponseDto::new)

                .toList();

    }


}