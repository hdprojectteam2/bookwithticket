package com.example.bookwithticket.domain.performance;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

//5. jpa , 대상 performance, 타입 long , 쿼리를 만들어서 DB로 날리고 받아와서 리턴
public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    List<Performance> findAllByOrderByIdDesc();
    //17. 찾아서 제목 반환 서비스로 다시
    List<Performance> findByTitleContainingOrderByIdDesc(String title);
    List<Performance> findByCategoryOrderByIdDesc(PerformanceCategory category);
    Optional<Performance> findById(Long id);
}
