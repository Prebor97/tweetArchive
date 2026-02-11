package com.example.tweetArchive.controller;

import com.example.tweetArchive.dto.request.DeletionCriteriaDto;
import com.example.tweetArchive.entities.EvaluationCriteria;
import com.example.tweetArchive.entities.Tweet;
import com.example.tweetArchive.jwt.JwtUtils;
import com.example.tweetArchive.repository.TweetRepository;
import com.example.tweetArchive.service.GrokAIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GrokAIController {
    private final GrokAIService grokAIService;
    private final JwtUtils jwtUtils;
    private final TweetRepository tweetRepository;
    @GetMapping("/ask")
    public String ask(@Valid @RequestBody DeletionCriteriaDto criteria,
                      @RequestHeader(name = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "Token not present";
        }
        String jwtToken = authHeader.substring(7);
        List<String> userInfo = jwtUtils.getUserInfo(jwtToken);
        String user_id = userInfo.get(0);
        Tweet tweet = tweetRepository.findFirstByUserId(user_id).orElseThrow();
        EvaluationCriteria evaluationCriteria = new EvaluationCriteria();
        evaluationCriteria.setCriteriaName(criteria.getCriteriaName());
        evaluationCriteria.setCriteriaList(criteria.getCriteriaList());
        evaluationCriteria.setUserId(user_id);
        evaluationCriteria.setCreatedDate(LocalDateTime.now());
        String response =  grokAIService.ask(evaluationCriteria.getCriteriaList().toString(),tweet);
        log.info("Tweet evaluated to: {}", response);
        return response;
    }
}
