package com.finpulse.finpulse_ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticSearchService {

    private final VectorStore vectorStore;

    public List<Document> findRelevantChunks(String query, int topK) {
        // Embeds the query and finds the topK most similar chunks in pgvector
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(0.5) // ignore chunks below 50% similarity
                        .build()
        );

        log.info("Found {} relevant chunks for query: '{}'", results.size(), query);
        return results;
    }
}