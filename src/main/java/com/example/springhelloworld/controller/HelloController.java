package com.example.springhelloworld.controller;

import com.example.springhelloworld.dto.NameRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hi")
public class HelloController {

    @GetMapping
    public ResponseEntity<String> getHello() {
        return ResponseEntity.ok("Hello");
    }

    @PostMapping
    public ResponseEntity<String> postHello(@RequestBody NameRequest request) {
        String response = "Hi " + request.getName();
        return ResponseEntity.ok(response);
    }
}

