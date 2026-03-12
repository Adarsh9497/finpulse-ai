package com.finpulse.finpulse_ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final SemanticSearchService semanticSearchService;

    private static final String SYSTEM_PROMPT = """
            You are FinPulse AI, an expert financial analyst assistant.
            You answer questions based ONLY on the provided context from financial documents.
            
            Rules:
            - If the answer is in the context, answer precisely and cite the page number.
            - If the answer is NOT in the context, say "I could not find this information in the uploaded documents."
            - Never make up numbers or financial data.
            - Always mention the source page when referencing specific figures.
            """;

    public String chat(String message) {
        // 1. Find relevant chunks from vector store
        List<Document> relevantChunks = semanticSearchService.findRelevantChunks(message, 5);

        // 2. If no relevant chunks found, fall back to general response
        if (relevantChunks.isEmpty()) {
            log.info("No relevant chunks found, responding without context");
            return chatClientBuilder.build()
                    .prompt()
                    .system(SYSTEM_PROMPT)
                    .user(message)
                    .call()
                    .content();
        }

        // 3. Format chunks into a readable context block with page citations
        String context = relevantChunks.stream()
                .map(doc -> String.format("[Page %s | %s]\n%s",
                        doc.getMetadata().getOrDefault("pageNumber", "N/A"),
                        doc.getMetadata().getOrDefault("sourceFile", "unknown"),
                        doc.getText()))
                .collect(Collectors.joining("\n\n---\n\n"));

        log.info("Sending {} chunks as context to LLM", relevantChunks.size());

        // 4. Send context + question to LLM
        return chatClientBuilder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(u -> u.text("""
                        Context from financial documents:
                        {context}
                        
                        Question: {question}
                        """)
                        .param("context", context)
                        .param("question", message))
                .call()
                .content();
    }
}