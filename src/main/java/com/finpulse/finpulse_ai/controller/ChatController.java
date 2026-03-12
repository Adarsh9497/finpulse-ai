package com.finpulse.finpulse_ai.controller;

import com.finpulse.finpulse_ai.dto.ChatRequest;
import com.finpulse.finpulse_ai.dto.ChatResponse;
import com.finpulse.finpulse_ai.dto.IngestRequest;
import com.finpulse.finpulse_ai.dto.IngestResponse;
import com.finpulse.finpulse_ai.service.ChatService;
import com.finpulse.finpulse_ai.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final IngestionService ingestionService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String answer = chatService.chat(request.message());
        return ResponseEntity.ok(new ChatResponse(answer));
    }

    @PostMapping("/ingest")
    public ResponseEntity<IngestResponse> ingest(@RequestBody IngestRequest request) {
        int chunks = ingestionService.ingestText(
                request.text(),
                request.sourceFile(),
                request.pageNumber(),
                request.chunkType()
        );
        return ResponseEntity.ok(new IngestResponse(chunks, request.sourceFile()));
    }
}