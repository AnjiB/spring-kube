# Local Testing Quick Reference

Quick commands for local development and testing with MySQL.

## Quick Start (Recommended: Docker Compose)

### Option 1: Run Everything with Docker Compose

**Start both MySQL and Spring Boot:**
```bash
docker compose up -d
```

This will:
- Start MySQL container
- Build and start Spring Boot application container
- Wait for MySQL to be healthy before starting Spring Boot
- Connect Spring Boot to MySQL automatically

**Test the API:**
```bash
# Create a book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": "B001",
    "bookName": "The Great Gatsby",
    "authorName": "F. Scott Fitzgerald"
  }'

# Get book by ID
curl http://localhost:8080/api/books/B001

# Get all books
curl http://localhost:8080/api/books
```

**View logs:**
```bash
# All services
docker compose logs -f

# Spring Boot only
docker compose logs -f spring-boot-app

# MySQL only
docker compose logs -f mysql
```

**Stop everything:**
```bash
docker compose down
```

**Stop and remove volumes (deletes database data):**
```bash
docker compose down -v
```

### Option 2: MySQL Only (Run Spring Boot Locally)

**Start MySQL:**
```bash
docker compose up -d mysql
```

**Run Spring Boot locally:**
```bash
mvn spring-boot:run
```

**Stop MySQL:**
```bash
docker compose down
```

## Alternative: Docker Run Commands

### Start MySQL
```bash
docker run -d \
  --name spring-mysql-local \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=springdb \
  -e MYSQL_USER=springuser \
  -e MYSQL_PASSWORD=springpassword \
  -p 3306:3306 \
  -v mysql-data:/var/lib/mysql \
  mysql:8.0
```

### Stop MySQL
```bash
docker stop spring-mysql-local
docker rm spring-mysql-local
```

## MySQL Access

### Connect to MySQL
```bash
# Using docker compose
docker compose exec mysql mysql -uroot -prootpassword springdb

# Using docker run
docker exec -it spring-mysql-local mysql -uroot -prootpassword springdb
```

### Useful MySQL Commands
```sql
SHOW DATABASES;
USE springdb;
SHOW TABLES;
DESCRIBE books;
SELECT * FROM books;
```

## Troubleshooting

### Check MySQL Status
```bash
docker ps | grep mysql
docker logs spring-mysql-local
```

### Check Port Availability
```bash
lsof -i :3306
```

### Reset Database
```bash
docker compose down -v
docker compose up -d mysql
```

## Database Configuration

- **Host**: localhost
- **Port**: 3306
- **Database**: springdb
- **Username**: root
- **Password**: rootpassword

Tables are created automatically by Hibernate when the application starts.

