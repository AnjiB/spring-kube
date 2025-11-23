# Swagger/OpenAPI Documentation Guide

This guide explains how to access and use the Swagger UI for API documentation.

## What is Swagger?

Swagger (OpenAPI) provides interactive API documentation that allows you to:
- View all available endpoints
- See request/response schemas
- Test API endpoints directly from the browser
- Understand API parameters and responses

## Accessing Swagger UI

### Local Development

1. Start the application:
   ```bash
   mvn spring-boot:run
   ```

2. Open Swagger UI in your browser:
   ```
   http://localhost:8080/swagger-ui.html
   ```

### Kubernetes Deployment

#### Using kubectl proxy:

1. Start kubectl proxy:
   ```bash
   kubectl proxy
   ```

2. Access Swagger UI:
   ```
   http://localhost:8001/api/v1/namespaces/default/services/spring-nodeport/proxy/swagger-ui.html
   ```

#### Using minikube service:

1. Get service URL:
   ```bash
   minikube service spring-nodeport --url
   ```

2. Append `/swagger-ui.html` to the URL:
   ```
   http://<service-url>/swagger-ui.html
   ```

## Accessing OpenAPI JSON

The OpenAPI specification in JSON format is available at:

**Local:**
```
http://localhost:8080/api-docs
```

**Kubernetes (via kubectl proxy):**
```
http://localhost:8001/api/v1/namespaces/default/services/spring-nodeport/proxy/api-docs
```

## Using Swagger UI

### 1. View API Endpoints

Swagger UI displays all endpoints organized by tags:
- **Hello**: Greeting endpoints (`/hi`)
- **Books**: Book CRUD operations (`/api/books`)

### 2. Test Endpoints

1. Click on an endpoint to expand it
2. Click "Try it out" button
3. Fill in the required parameters
4. Click "Execute"
5. View the response below

### 3. Example: Create a Book

1. Navigate to **Books** section
2. Click on `POST /api/books`
3. Click "Try it out"
4. Enter request body:
   ```json
   {
     "bookId": "B001",
     "bookName": "The Great Gatsby",
     "authorName": "F. Scott Fitzgerald"
   }
   ```
5. Click "Execute"
6. View the response (201 Created)

### 4. Example: Get Book by ID

1. Navigate to **Books** section
2. Click on `GET /api/books/{bookId}`
3. Click "Try it out"
4. Enter `bookId`: `B001`
5. Click "Execute"
6. View the response (200 OK with book details)

## API Documentation Features

### Request/Response Schemas

Each endpoint shows:
- **Parameters**: Path, query, and request body parameters
- **Request Body**: Example JSON with required fields
- **Responses**: All possible response codes and their schemas
- **Examples**: Sample request/response data

### Data Models

The **Schemas** section at the bottom shows all data models:
- `BookRequest`: Request DTO for creating/updating books
- `BookResponse`: Response DTO for book data
- `NameRequest`: Request DTO for personalized greeting

## Configuration

Swagger is configured in:
- **OpenApiConfig.java**: Main configuration with API info and servers
- **application.properties**: Swagger UI path configuration

### Customization

You can customize Swagger by editing `OpenApiConfig.java`:
- Change API title, description, version
- Add contact information
- Add more servers (dev, test, prod)
- Configure security schemes

## Troubleshooting

### Swagger UI not loading

1. Check if the application is running:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. Check if Swagger endpoint is accessible:
   ```bash
   curl http://localhost:8080/api-docs
   ```

3. Check application logs for errors

### CORS Issues

If accessing Swagger UI from a different origin, you may need to configure CORS in your Spring Boot application.

### Kubernetes Access Issues

- Ensure the service is running: `kubectl get services`
- Check pod status: `kubectl get pods`
- Verify port forwarding or service URL

## Exporting API Documentation

### Export OpenAPI JSON

```bash
# Local
curl http://localhost:8080/api-docs > openapi.json

# Kubernetes
curl http://localhost:8001/api/v1/namespaces/default/services/spring-nodeport/proxy/api-docs > openapi.json
```

### Import to Postman

1. Export OpenAPI JSON (see above)
2. Open Postman
3. Click **Import**
4. Select the `openapi.json` file
5. All endpoints will be imported as a collection

## Best Practices

1. **Keep Documentation Updated**: Update Swagger annotations when changing APIs
2. **Add Examples**: Include example values in `@Schema` annotations
3. **Describe Endpoints**: Use `@Operation` annotation with clear descriptions
4. **Document Errors**: Use `@ApiResponses` to document error responses
5. **Use Tags**: Organize endpoints with `@Tag` annotations

## Additional Resources

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)

