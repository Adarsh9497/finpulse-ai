package com.finpulse.finpulse_ai.controller;

import com.finpulse.finpulse_ai.dto.ChatRequest;
import com.finpulse.finpulse_ai.dto.ChatResponse;
import com.finpulse.finpulse_ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String answer = chatService.chat(request.message());
        return ResponseEntity.ok(new ChatResponse(answer));
    }
}
