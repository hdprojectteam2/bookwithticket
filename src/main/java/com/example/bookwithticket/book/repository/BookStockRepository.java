package com.example.bookwithticket.book.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bookwithticket.book.entity.Book;

public interface BookStockRepository extends JpaRepository<Book, Long> {

	@Modifying
    @Query("""
        update Book b
           set b.stock = b.stock - :quantity
         where b.id = :bookId
           and b.stock >= :quantity
    """)
    int decreaseStock(
            @Param("bookId") Long bookId,
            @Param("quantity") int quantity
    );

    @Modifying
    @Query("""
        update Book b
           set b.stock = b.stock + :quantity
         where b.id = :bookId
    """)
    int increaseStock(
            @Param("bookId") Long bookId,
            @Param("quantity") int quantity
    );

}