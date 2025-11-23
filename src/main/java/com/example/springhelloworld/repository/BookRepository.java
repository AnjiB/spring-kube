package com.example.springhelloworld.repository;

import com.example.springhelloworld.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    /**
     * Find a book by bookId
     * @param bookId the book ID to search for
     * @return Optional containing the book if found
     */
    Optional<Book> findByBookId(String bookId);
    
    /**
     * Check if a book exists by bookId
     * @param bookId the book ID to check
     * @return true if book exists, false otherwise
     */
    boolean existsByBookId(String bookId);
}

