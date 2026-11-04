package com.excel.chatbot.controller;

import com.excel.chatbot.model.ChatRequest;
import com.excel.chatbot.model.ChatResponse;
import com.excel.chatbot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.processMessage(
                request.getMessage(),
                request.getCategory(),
                request.getContext()
        );
        return ResponseEntity.ok(response);
    }
}

