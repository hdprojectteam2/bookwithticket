package com.example.bookwithticket.member.repository;

import com.example.bookwithticket.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("select count(m) from Member m where m.isActive = true")
    long countActiveMembers();
}
