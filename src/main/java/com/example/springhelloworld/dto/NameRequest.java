package com.example.springhelloworld.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for personalized greeting")
public class NameRequest {
    private String name;

    public NameRequest() {
    }

    public NameRequest(String name) {
        this.name = name;
    }

    @Schema(description = "Name for personalized greeting", example = "Anji", requiredMode = Schema.RequiredMode.REQUIRED)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

