package com.finpulse.finpulse_ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final VectorStore vectorStore;

    // Splits text into chunks of ~500 tokens with 50 token overlap between chunks
    // Overlap ensures context isn't lost at chunk boundaries
    private final TokenTextSplitter splitter = new TokenTextSplitter(500, 50, 10, 10000, true);

    public int ingestText(String text, String sourceFile, int pageNumber, String chunkType) {
        // 1. Wrap raw text in a Spring AI Document with metadata
        Document doc = new Document(text, Map.of(
                "sourceFile", sourceFile,
                "pageNumber", pageNumber,
                "chunkType", chunkType   // "TEXT" or "TABLE"
        ));

        // 2. Split into smaller chunks
        List<Document> chunks = splitter.apply(List.of(doc));

        // 3. Embed each chunk and store in pgvector (Spring AI handles this in one call)
        vectorStore.add(chunks);

        log.info("Ingested {} chunks from {} (page {}, type: {})",
                chunks.size(), sourceFile, pageNumber, chunkType);

        return chunks.size();
    }
}