package com.finpulse.finpulse_ai.service;

import com.finpulse.finpulse_ai.config.RabbitMQConfig;
import com.finpulse.finpulse_ai.dto.FileIngestionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter = new TokenTextSplitter(500, 50, 10, 10000, true);

    @RabbitListener(queues = RabbitMQConfig.INGESTION_QUEUE)
    public void processFile(FileIngestionMessage message) {
        log.info("Starting async ingestion for file: {}", message.originalFilename());
        File file = new File(message.filePath());
        
        if (!file.exists()) {
            log.error("File not found for ingestion: {}", message.filePath());
            return;
        }

        try {
            // 1. Read document using Tika
            TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(file));
            List<Document> rawDocuments = reader.get();

            // 2. Add metadata to all extracted documents
            rawDocuments.forEach(doc -> {
                doc.getMetadata().put("sourceFile", message.originalFilename());
                doc.getMetadata().put("chunkType", message.chunkType());
            });

            // 3. Split into chunks
            List<Document> chunks = splitter.apply(rawDocuments);

            // 4. Embed and store
            vectorStore.add(chunks);

            log.info("Successfully ingested {} chunks from {}", chunks.size(), message.originalFilename());
        } catch (Exception e) {
            log.error("Failed to process file: {}", message.originalFilename(), e);
        } finally {
            // Cleanup temp file
            if (file.exists() && !file.delete()) {
                log.warn("Failed to delete temp file: {}", file.getAbsolutePath());
            }
        }
    }
}