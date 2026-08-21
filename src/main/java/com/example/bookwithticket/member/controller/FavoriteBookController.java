package com.example.bookwithticket.member.controller;

import com.example.bookwithticket.member.dto.FavoriteBookResponseDto;
import com.example.bookwithticket.member.entity.Member;
import com.example.bookwithticket.member.service.FavoriteBookService;
import com.example.bookwithticket.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/members/favorites")
public class FavoriteBookController {
    private final FavoriteBookService favoriteBookService;
    private final MemberService memberService;

    public FavoriteBookController(FavoriteBookService favoriteBookService, MemberService memberService) {
        this.favoriteBookService = favoriteBookService;
        this.memberService = memberService;
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<FavoriteBookResponseDto> add(@PathVariable Long bookId, Authentication authentication) {
        Member member = memberService.findMyInfo(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FavoriteBookResponseDto(favoriteBookService.addFavorite(member, bookId)));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> delete(@PathVariable Long bookId, Authentication authentication) {
        Member member = memberService.findMyInfo(authentication.getName());
        favoriteBookService.removeFavorite(member, bookId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<FavoriteBookResponseDto> findAll(Authentication authentication) {
        Member member = memberService.findMyInfo(authentication.getName());
        return favoriteBookService.findFavorites(member).stream()
                .map(FavoriteBookResponseDto::new)
                .toList();
    }
}
