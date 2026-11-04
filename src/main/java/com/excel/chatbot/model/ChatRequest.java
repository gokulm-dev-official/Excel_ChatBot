package com.excel.chatbot.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatRequest {
    @NotBlank(message = "Message cannot be empty")
    @Size(max = 1000, message = "Message too long")
    private String message;
    private String category;
    private String context;

    public ChatRequest() {}

    public ChatRequest(String message, String category, String context) {
        this.message = message;
        this.category = category;
        this.context = context;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}

