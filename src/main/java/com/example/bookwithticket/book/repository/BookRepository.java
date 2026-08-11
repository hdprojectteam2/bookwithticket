package com.example.bookwithticket.book.repository;

import com.example.bookwithticket.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);
    Optional<Book> findByIsbn(String isbn);

    @Query("""
        select b from Book b
        where (
            :keyword is null
            or lower(b.title) like lower(concat('%', :keyword, '%'))
            or lower(coalesce(b.author, '')) like lower(concat('%', :keyword, '%'))
            or lower(coalesce(b.publisher, '')) like lower(concat('%', :keyword, '%'))
            or b.isbn like concat('%', :keyword, '%')
        )
        and (:category is null or b.category = :category)
        """)
    Page<Book> search(@Param("keyword") String keyword,
                      @Param("category") String category,
                      Pageable pageable);

    List<Book> findTop5ByTitleContainingIgnoreCaseOrderByViewCountDesc(String keyword);
    List<Book> findTop10ByOrderByViewCountDescIdDesc();
    List<Book> findTop10ByOrderBySalesCountDescIdDesc();
    List<Book> findTop10ByOrderByPublishedDateDescIdDesc();

    // 기존 dev 호출부 호환
    List<Book> findByTitleContaining(String keyword);
    List<Book> findByCategory(String category);
    List<Book> findTop5ByTitleContaining(String keyword);
    List<Book> findTop10ByOrderByViewCountDesc();
    List<Book> findTop10ByOrderByCreatedAtDesc();
}
