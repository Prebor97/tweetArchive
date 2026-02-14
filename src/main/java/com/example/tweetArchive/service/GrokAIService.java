package com.example.tweetArchive.service;

import com.example.tweetArchive.entities.Tweet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrokAIService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.groq.com/openai/v1")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicInteger apiCallCounter = new AtomicInteger(0);

    public void resetCounter() {
        int prev = apiCallCounter.getAndSet(0);
        log.info("Groq API call counter reset (was {})", prev);
    }

    public String ask(String criteria, Tweet tweet) {
        int callNumber = apiCallCounter.incrementAndGet();

        // Throttle: sleep before every 30th call (after 29, 58, 87, ...)
        if (callNumber % 29 == 0) {
            log.info("Groq call #{} → sleeping 60 seconds (rate limit protection)", callNumber);
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Sleep interrupted", e);
            }
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", new Object[]{
                            Map.of("role", "user", "content",
                                    criteria + ". These are the list of criteria i want" +
                                            "you to evaluate the tweet below. If it matches the criteria for" +
                                            " deletion please answer only true" +
                                            "and nothing else but if it does not meet the criteria answer only false and " +
                                            "nothing else. Below is the tweet in question :" +
                                            tweet.getCleanedTweet())
                    },
                    "temperature", 0.7,
                    "max_tokens", 2048
            ));

            String response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0)
                    .path("message").path("content").asText().trim();

        } catch (Exception e) {
            log.error("Groq call #{} failed for tweet {}: {}",
                    callNumber, tweet.getTweetId(), e.getMessage(), e);
            throw new RuntimeException("Groq API call failed", e);
        }
    }
}