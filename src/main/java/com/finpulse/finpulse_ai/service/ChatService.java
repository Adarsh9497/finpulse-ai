package com.finpulse.finpulse_ai.service;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient.Builder chatClientBuilder;

    public String chat(String message) {
        ChatClient chatClient = chatClientBuilder
                .defaultSystem("You are FinPulse AI, an expert financial analyst assistant. " +
                        "You help users analyze financial reports, extract key metrics, " +
                        "and provide data-driven insights.")
                .build();

        return chatClient
                .prompt()
                .user(message)
                .call()
                .content();
    }
}