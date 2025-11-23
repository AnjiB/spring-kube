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
- kubectl (for Kubernetes deployment)
- Minikube or Kind (for local Kubernetes clusters)

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

### Quick Reference
- **[Commands Reference](docs/COMMANDS.md)** - Complete command reference for Docker, Minikube, Kind, kubectl, and Kubeshark

### Getting Started
- **[Local Testing Guide](docs/LOCAL-TESTING.md)** - Complete guide for local development and testing
- **[Book API Guide](docs/BOOK-API-GUIDE.md)** - Detailed Book CRUD API documentation with examples

### Docker & Containerization
- **[Docker Compose](docker-compose.yml)** - Local development setup with MySQL and Spring Boot

### Kubernetes Deployment
- **[Kind Multi-Cluster Deployment](docs/KIND-MULTI-CLUSTER-DEPLOYMENT.md)** - Deploying to multiple Kind clusters (dev/test) - **Recommended for Practice**
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
│   ├── COMMANDS.md                # Complete command reference
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
│   ├── kind-dev-cluster-config.yml
│   ├── kind-test-cluster-config.yml
│   ├── multiple-services-example.yml
│   ├── mysql-service.yml
│   ├── spring-boot-deployment.yml
│   ├── spring-boot-service.yml
│   ├── spring-boot-with-mysql-sidecar.yml
│   └── spring-boot-with-shared-mysql.yml
├── scripts/                       # Deployment scripts
│   ├── deploy-to-clusters.sh
│   └── get-cluster-urls.sh
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
- Kubernetes (Minikube, Kind)
- kubectl
- SpringDoc OpenAPI (Swagger)

## Quick Reference

For all commands (Docker, Minikube, Kind, kubectl, Kubeshark), see **[Commands Reference](docs/COMMANDS.md)**.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the Apache License 2.0.
