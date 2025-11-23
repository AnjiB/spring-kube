package com.example.springhelloworld.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for creating or updating a book")
public class BookRequest {
    private String bookId;
    private String bookName;
    private String authorName;
    
    // Default constructor
    public BookRequest() {
    }
    
    // Constructor with all fields
    public BookRequest(String bookId, String bookName, String authorName) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.authorName = authorName;
    }
    
    // Getters and Setters
    @Schema(description = "Unique identifier for the book", example = "B001", requiredMode = Schema.RequiredMode.REQUIRED)
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    @Schema(description = "Name of the book", example = "The Great Gatsby", requiredMode = Schema.RequiredMode.REQUIRED)
    public String getBookName() {
        return bookName;
    }
    
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
    
    @Schema(description = "Name of the author", example = "F. Scott Fitzgerald", requiredMode = Schema.RequiredMode.REQUIRED)
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}

