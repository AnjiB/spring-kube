package com.example.springhelloworld.controller;

import com.example.springhelloworld.dto.NameRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hi")
@CrossOrigin(origins = "*")
@Tag(name = "Hello", description = "Simple greeting API endpoints")
public class HelloController {

    @GetMapping
    @Operation(
            summary = "Get greeting",
            description = "Returns a simple 'Hello' greeting message"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved greeting",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Hello")
                    )
            )
    })
    public ResponseEntity<String> getHello() {
        return ResponseEntity.ok("Hello");
    }

    @PostMapping
    @Operation(
            summary = "Post personalized greeting",
            description = "Accepts a name in the request body and returns a personalized greeting"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully created personalized greeting",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Hi Anji")
                    )
            )
    })
    public ResponseEntity<String> postHello(@RequestBody NameRequest request) {
        String response = "Hi " + request.getName();
        return ResponseEntity.ok(response);
    }
}

