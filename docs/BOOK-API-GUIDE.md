# Book CRUD API Guide

This guide explains how to use the Book CRUD API endpoints.

## API Endpoints

Base URL: `http://localhost:8080/api/books`

### 1. Create a Book
**POST** `/api/books`

Creates a new book with bookId, bookName, and authorName.

**Request Body:**
```json
{
  "bookId": "B001",
  "bookName": "The Great Gatsby",
  "authorName": "F. Scott Fitzgerald"
}
```

**Response (201 Created):**
```json
{
  "bookId": "B001",
  "bookName": "The Great Gatsby",
  "authorName": "F. Scott Fitzgerald"
}
```

**Example using curl:**
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": "B001",
    "bookName": "The Great Gatsby",
    "authorName": "F. Scott Fitzgerald"
  }'
```

### 2. Get Book by bookId
**GET** `/api/books/{bookId}`

Retrieves a book by its bookId.

**Response (200 OK):**
```json
{
  "bookId": "B001",
  "bookName": "The Great Gatsby",
  "authorName": "F. Scott Fitzgerald"
}
```

**Response (404 Not Found):**
```
Book with ID 'B001' not found
```

**Example using curl:**
```bash
curl http://localhost:8080/api/books/B001
```

### 3. Get All Books
**GET** `/api/books`

Retrieves all books in the database.

**Response (200 OK):**
```json
[
  {
    "bookId": "B001",
    "bookName": "The Great Gatsby",
    "authorName": "F. Scott Fitzgerald"
  },
  {
    "bookId": "B002",
    "bookName": "To Kill a Mockingbird",
    "authorName": "Harper Lee"
  }
]
```

**Example using curl:**
```bash
curl http://localhost:8080/api/books
```

### 4. Update a Book
**PUT** `/api/books/{bookId}`

Updates the bookName and authorName of an existing book.

**Request Body:**
```json
{
  "bookName": "The Great Gatsby (Updated)",
  "authorName": "F. Scott Fitzgerald"
}
```

**Response (200 OK):**
```json
{
  "bookId": "B001",
  "bookName": "The Great Gatsby (Updated)",
  "authorName": "F. Scott Fitzgerald"
}
```

**Example using curl:**
```bash
curl -X PUT http://localhost:8080/api/books/B001 \
  -H "Content-Type: application/json" \
  -d '{
    "bookName": "The Great Gatsby (Updated)",
    "authorName": "F. Scott Fitzgerald"
  }'
```

### 5. Delete a Book
**DELETE** `/api/books/{bookId}`

Deletes a book by its bookId.

**Response (200 OK):**
```
Book with ID 'B001' deleted successfully
```

**Example using curl:**
```bash
curl -X DELETE http://localhost:8080/api/books/B001
```

## Complete Example Workflow

```bash
# 1. Create a book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": "B001",
    "bookName": "The Great Gatsby",
    "authorName": "F. Scott Fitzgerald"
  }'

# 2. Get the book by bookId
curl http://localhost:8080/api/books/B001

# 3. Create another book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": "B002",
    "bookName": "To Kill a Mockingbird",
    "authorName": "Harper Lee"
  }'

# 4. Get all books
curl http://localhost:8080/api/books

# 5. Update a book
curl -X PUT http://localhost:8080/api/books/B001 \
  -H "Content-Type: application/json" \
  -d '{
    "bookName": "The Great Gatsby (Revised Edition)",
    "authorName": "F. Scott Fitzgerald"
  }'

# 6. Delete a book
curl -X DELETE http://localhost:8080/api/books/B002

# 7. Verify deletion
curl http://localhost:8080/api/books
```

## Using with kubectl proxy

If you're accessing via kubectl proxy:

```bash
# Start proxy
kubectl proxy

# Create a book
curl -X POST http://localhost:8001/api/v1/namespaces/default/services/spring-nodeport/proxy/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": "B001",
    "bookName": "The Great Gatsby",
    "authorName": "F. Scott Fitzgerald"
  }'

# Get book by bookId
curl http://localhost:8001/api/v1/namespaces/default/services/spring-nodeport/proxy/api/books/B001
```

## Error Responses

### 400 Bad Request
Missing required fields:
```json
"Book ID is required"
"Book name is required"
"Author name is required"
```

### 404 Not Found
Book doesn't exist:
```
Book with ID 'B001' not found
```

### 409 Conflict
Book with same bookId already exists:
```
Book with ID 'B001' already exists
```

## Database Schema

The `books` table is automatically created with the following structure:

```sql
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id VARCHAR(255) UNIQUE NOT NULL,
    book_name VARCHAR(255) NOT NULL,
    author_name VARCHAR(255) NOT NULL
);
```

## Testing the API

### Using Postman

1. Import the Postman collection (if available)
2. Or manually create requests:
   - **POST** `http://localhost:8080/api/books`
   - **GET** `http://localhost:8080/api/books/{bookId}`
   - **GET** `http://localhost:8080/api/books`
   - **PUT** `http://localhost:8080/api/books/{bookId}`
   - **DELETE** `http://localhost:8080/api/books/{bookId}`

### Verify Database

You can verify the data in MySQL:

```bash
# Get pod name
POD_NAME=$(kubectl get pods -l app=spring-hello-world -o jsonpath='{.items[0].metadata.name}')

# Connect to MySQL
kubectl exec -it $POD_NAME -c mysql -- mysql -uroot -prootpassword springdb

# Run SQL queries
SHOW TABLES;
SELECT * FROM books;
DESCRIBE books;
```

## Notes

- `bookId` must be unique
- All fields (bookId, bookName, authorName) are required when creating a book
- When updating, only bookName and authorName can be changed (bookId cannot be changed)
- The database table is automatically created on first run (via JPA `ddl-auto=update`)

