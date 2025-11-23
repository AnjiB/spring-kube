package com.example.springhelloworld.controller;

import com.example.springhelloworld.dto.BookRequest;
import com.example.springhelloworld.dto.BookResponse;
import com.example.springhelloworld.entity.Book;
import com.example.springhelloworld.repository.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
@Tag(name = "Books", description = "Book CRUD operations API")
public class BookController {
    
    @Autowired
    private BookRepository bookRepository;
    
    /**
     * Create a new book
     * POST /api/books
     */
    @PostMapping
    @Operation(
            summary = "Create a new book",
            description = "Creates a new book with bookId, bookName, and authorName. BookId must be unique."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - missing required fields"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Book with the same bookId already exists"
            )
    })
    public ResponseEntity<?> createBook(@RequestBody BookRequest bookRequest) {
        // Validate input
        if (bookRequest.getBookId() == null || bookRequest.getBookId().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Book ID is required");
        }
        if (bookRequest.getBookName() == null || bookRequest.getBookName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Book name is required");
        }
        if (bookRequest.getAuthorName() == null || bookRequest.getAuthorName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Author name is required");
        }
        
        // Check if book with same bookId already exists
        if (bookRepository.existsByBookId(bookRequest.getBookId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Book with ID '" + bookRequest.getBookId() + "' already exists");
        }
        
        // Create and save book
        Book book = new Book(
                bookRequest.getBookId(),
                bookRequest.getBookName(),
                bookRequest.getAuthorName()
        );
        
        Book savedBook = bookRepository.save(book);
        BookResponse response = new BookResponse(
                savedBook.getBookId(),
                savedBook.getBookName(),
                savedBook.getAuthorName()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get book by bookId
     * GET /api/books/{bookId}
     */
    @GetMapping("/{bookId}")
    @Operation(
            summary = "Get book by ID",
            description = "Retrieves a book by its unique bookId. Returns book name and author name."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book found",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found"
            )
    })
    public ResponseEntity<?> getBookByBookId(@PathVariable String bookId) {
        Optional<Book> bookOptional = bookRepository.findByBookId(bookId);
        
        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            BookResponse response = new BookResponse(
                    book.getBookId(),
                    book.getBookName(),
                    book.getAuthorName()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Book with ID '" + bookId + "' not found");
        }
    }
    
    /**
     * Get all books
     * GET /api/books
     */
    @GetMapping
    @Operation(
            summary = "Get all books",
            description = "Retrieves a list of all books in the database"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of books",
            content = @Content(schema = @Schema(implementation = BookResponse.class))
    )
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        List<BookResponse> responses = books.stream()
                .map(book -> new BookResponse(
                        book.getBookId(),
                        book.getBookName(),
                        book.getAuthorName()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Update a book by bookId
     * PUT /api/books/{bookId}
     */
    @PutMapping("/{bookId}")
    @Operation(
            summary = "Update a book",
            description = "Updates the bookName and authorName of an existing book. BookId cannot be changed."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book updated successfully",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - missing required fields"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found"
            )
    })
    public ResponseEntity<?> updateBook(@PathVariable String bookId, 
                                       @RequestBody BookRequest bookRequest) {
        Optional<Book> bookOptional = bookRepository.findByBookId(bookId);
        
        if (bookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Book with ID '" + bookId + "' not found");
        }
        
        // Validate input
        if (bookRequest.getBookName() == null || bookRequest.getBookName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Book name is required");
        }
        if (bookRequest.getAuthorName() == null || bookRequest.getAuthorName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Author name is required");
        }
        
        Book book = bookOptional.get();
        book.setBookName(bookRequest.getBookName());
        book.setAuthorName(bookRequest.getAuthorName());
        
        Book updatedBook = bookRepository.save(book);
        BookResponse response = new BookResponse(
                updatedBook.getBookId(),
                updatedBook.getBookName(),
                updatedBook.getAuthorName()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a book by bookId
     * DELETE /api/books/{bookId}
     */
    @DeleteMapping("/{bookId}")
    @Operation(
            summary = "Delete a book",
            description = "Deletes a book by its bookId"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found"
            )
    })
    public ResponseEntity<?> deleteBook(@PathVariable String bookId) {
        Optional<Book> bookOptional = bookRepository.findByBookId(bookId);
        
        if (bookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Book with ID '" + bookId + "' not found");
        }
        
        bookRepository.delete(bookOptional.get());
        return ResponseEntity.ok("Book with ID '" + bookId + "' deleted successfully");
    }
}

