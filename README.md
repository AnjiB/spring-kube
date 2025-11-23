# Spring Hello World API

A simple Spring Boot REST API application with Book CRUD operations and MySQL database integration.

## Features

- **GET `/hi`** - Returns "Hello"
- **POST `/hi`** - Accepts a name in the request body and returns a personalized greeting
- **Book CRUD API** - Full CRUD operations for books with MySQL database
- **Swagger/OpenAPI Documentation** - Interactive API documentation
- **Kubernetes Ready** - Deployment configurations for Kubernetes

## Prerequisites

- Java 17 or higher
- Maven 3.6+ (or use Maven wrapper)
- Docker (for containerized deployment and local MySQL)
- Docker Compose (optional, for easier local MySQL setup)
- Minikube and kubectl (for Kubernetes deployment)

## Quick Start

### Local Development with Docker Compose

The easiest way to get started:

```bash
# Start MySQL and Spring Boot
docker compose up -d

# View logs
docker compose logs -f

# Test the API
curl http://localhost:8080/api/books

# Access Swagger UI
open http://localhost:8080/swagger-ui.html

# Stop everything
docker compose down
```

### Local Development (Maven)

```bash
# Start MySQL
docker compose up -d mysql

# Run Spring Boot
mvn spring-boot:run

# Test the API
curl http://localhost:8080/hi
```

### Building the Project

```bash
mvn clean package
```

This will create a JAR file in the `target` directory: `spring-hello-world-1.0.0.jar`

## API Endpoints

### Hello Endpoints

- **GET `/hi`** - Returns "Hello"
- **POST `/hi`** - Accepts `{"name": "Anji"}` and returns "Hi Anji"

### Book CRUD Endpoints

- **POST `/api/books`** - Create a new book
- **GET `/api/books/{bookId}`** - Get book by ID
- **GET `/api/books`** - Get all books
- **PUT `/api/books/{bookId}`** - Update a book
- **DELETE `/api/books/{bookId}`** - Delete a book

### API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

## Documentation

Detailed documentation is available in the `docs/` folder:

### Getting Started
- **[Local Testing Guide](docs/LOCAL-TESTING.md)** - Complete guide for local development and testing
- **[Book API Guide](docs/BOOK-API-GUIDE.md)** - Detailed Book CRUD API documentation with examples

### Docker & Containerization
- **[Docker Compose](docker-compose.yml)** - Local development setup with MySQL and Spring Boot

### Kubernetes Deployment
- **[Minikube Deployment](docs/MULTI-CLUSTER-GUIDE.md)** - Deploying to Minikube with multiple clusters
- **[NodePort Service Deployment](kube/spring-boot-service.yml)** - Using NodePort service for external access
- **[Port Forwarding Guide](docs/PORT-FORWARDING-GUIDE.md)** - Accessing services via port forwarding (recommended for development)
- **[kubectl Proxy Guide](docs/KUBECTL-PROXY-GUIDE.md)** - Accessing services via kubectl proxy
- **[Kubernetes DNS Guide](docs/KUBERNETES-DNS-GUIDE.md)** - Understanding service names and DNS resolution
- **[Troubleshooting Guide](docs/TROUBLESHOOTING.md)** - Common issues and solutions

### Database
- **[MySQL Sidecar Pattern](docs/SIDECAR-MYSQL-GUIDE.md)** - Deploying MySQL as sidecar container
- **[Database Consistency Guide](docs/DATABASE-CONSISTENCY-GUIDE.md)** - Understanding data consistency with multiple pods
- **[Shared MySQL Service](kube/mysql-service.yml)** - Production-ready shared MySQL deployment

### API Documentation
- **[Swagger/OpenAPI Guide](docs/SWAGGER-GUIDE.md)** - Using Swagger UI for API testing and documentation

## Project Structure

```
spring-hello-world/
├── docs/                          # Documentation guides
│   ├── BOOK-API-GUIDE.md
│   ├── DATABASE-CONSISTENCY-GUIDE.md
│   ├── KUBECTL-PROXY-GUIDE.md
│   ├── KUBERNETES-DNS-GUIDE.md
│   ├── LOCAL-TESTING.md
│   ├── MULTI-CLUSTER-GUIDE.md
│   ├── PORT-FORWARDING-GUIDE.md
│   ├── SIDECAR-MYSQL-GUIDE.md
│   ├── SWAGGER-GUIDE.md
│   └── TROUBLESHOOTING.md
├── kube/                          # Kubernetes deployment files
│   ├── deployment.yml
│   ├── multiple-services-example.yml
│   ├── mysql-service.yml
│   ├── spring-boot-deployment.yml
│   ├── spring-boot-service.yml
│   ├── spring-boot-with-mysql-sidecar.yml
│   └── spring-boot-with-shared-mysql.yml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/springhelloworld/
│       │       ├── SpringHelloWorldApplication.java
│       │       ├── controller/
│       │       │   ├── HelloController.java
│       │       │   └── BookController.java
│       │       ├── entity/
│       │       │   └── Book.java
│       │       ├── repository/
│       │       │   └── BookRepository.java
│       │       ├── dto/
│       │       │   ├── NameRequest.java
│       │       │   ├── BookRequest.java
│       │       │   └── BookResponse.java
│       │       └── config/
│       │           ├── CorsConfig.java
│       │           ├── CorsFilterConfig.java
│       │           └── OpenApiConfig.java
│       └── resources/
│           └── application.properties
├── docker-compose.yml             # Local development setup
├── Dockerfile                     # Docker image build
├── pom.xml                        # Maven dependencies
├── Spring-Hello-World.postman_collection.json
└── README.md                      # This file
```

## Technologies Used

- Spring Boot 3.4.5
- Java 17
- Maven
- MySQL 8.0
- Docker & Docker Compose
- Kubernetes (Minikube)
- SpringDoc OpenAPI (Swagger)

## Quick Reference

### Docker Commands
```bash
# Build image
docker build -t anji-spring-hello-world:latest .

# Run container
docker run -p 8080:8080 anji-spring-hello-world:latest

# Docker Compose
docker compose up -d              # Start all services
docker compose logs -f            # View logs
docker compose down               # Stop all services
```

### Kubernetes Commands
```bash
# Deploy to Minikube
minikube image load anji-spring-hello-world:latest
kubectl apply -f kube/spring-boot-service.yml

# Check status
kubectl get pods
kubectl get services

# Access via kubectl proxy
kubectl proxy
curl http://localhost:8001/api/v1/namespaces/default/services/spring-nodeport/proxy/hi

# List/remove images in minikube
minikube image ls
minikube image rm <imageName>
```

### How to Update Image with New Changes

When you make code changes and need to update the deployed application:

```bash
# 1. Rebuild the application
mvn clean package

# 2. Rebuild Docker image
docker build -t anji-spring-hello-world:latest .

# 3. Load new image into minikube
minikube image load anji-spring-hello-world:latest

# 4. Restart deployment to use new image
kubectl rollout restart deployment/spring-hello-world

# 5. Check rollout status
kubectl rollout status deployment/spring-hello-world

# 6. Verify pods are running with new image
kubectl get pods -l app=spring-hello-world

# 7. View logs to verify new changes
kubectl logs -f deployment/spring-hello-world
```

**Alternative: Force delete and recreate pods (if rollout restart doesn't work):**
```bash
# Delete existing pods (deployment will recreate them with new image)
kubectl delete pods -l app=spring-hello-world

# Watch pods being recreated
kubectl get pods -l app=spring-hello-world -w
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the Apache License 2.0.
