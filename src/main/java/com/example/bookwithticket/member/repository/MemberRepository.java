package com.example.bookwithticket.member.repository;

import com.example.bookwithticket.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}