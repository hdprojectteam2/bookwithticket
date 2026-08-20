package com.example.bookwithticket.domain.performance;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    List<Performance> findAllByOrderByIdDesc();
    List<Performance> findByActiveTrueOrderByIdDesc();

    List<Performance> findByTitleContainingOrderByIdDesc(String title);
    List<Performance> findByTitleContainingAndActiveTrueOrderByIdDesc(String title);

    List<Performance> findByCategoryOrderByIdDesc(PerformanceCategory category);
    List<Performance> findByCategoryAndActiveTrueOrderByIdDesc(PerformanceCategory category);

    Optional<Performance> findById(Long id);
}
