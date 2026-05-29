package com.finpulse.finpulse_ai.controller;

import com.finpulse.finpulse_ai.config.RabbitMQConfig;
import com.finpulse.finpulse_ai.dto.ChatRequest;
import com.finpulse.finpulse_ai.dto.ChatResponse;
import com.finpulse.finpulse_ai.dto.FileIngestionMessage;
import com.finpulse.finpulse_ai.dto.IngestResponse;
import com.finpulse.finpulse_ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String answer = chatService.chat(request.message());
        return ResponseEntity.ok(new ChatResponse(answer));
    }

    @PostMapping(value = "/ingest", consumes = {"multipart/form-data"})
    public ResponseEntity<IngestResponse> ingest(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chunkType") String chunkType) {
        
        try {
            // Ensure uploads directory exists
            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Save file temporarily
            String originalFilename = file.getOriginalFilename();
            File destFile = new File(uploadDir, System.currentTimeMillis() + "_" + originalFilename);
            file.transferTo(destFile);

            // Publish message to RabbitMQ
            FileIngestionMessage message = new FileIngestionMessage(
                    destFile.getAbsolutePath(),
                    originalFilename,
                    chunkType
            );
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INGESTION_EXCHANGE,
                    RabbitMQConfig.INGESTION_ROUTING_KEY,
                    message
            );
            
            log.info("Queued file for processing: {}", originalFilename);

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new IngestResponse("File queued for async processing.", originalFilename));

        } catch (IOException e) {
            log.error("Failed to save uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new IngestResponse("Failed to process upload", "unknown"));
        }
    }
}