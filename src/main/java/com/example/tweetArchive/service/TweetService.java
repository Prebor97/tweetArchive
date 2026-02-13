package com.example.tweetArchive.service;

import com.example.tweetArchive.dto.response.TweetResponse;
import com.example.tweetArchive.dto.twitterDto.TweetDto;
import com.example.tweetArchive.dto.twitterDto.TweetJson;
import com.example.tweetArchive.dto.twitterDto.TweetJsonWrapper;
import com.example.tweetArchive.entities.EvaluationCriteria;
import com.example.tweetArchive.entities.Tweet;
import com.example.tweetArchive.exception.UserNotFoundException;
import com.example.tweetArchive.repository.EvaluationCriteriaRepository;
import com.example.tweetArchive.repository.TweetRepository;
import com.example.tweetArchive.repository.UserInfoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TweetService {
    private final TweetRepository tweetRepository;
    private final EvaluationCriteriaRepository evaluationCriteriaRepository;

    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);

    public Page<TweetDto> getFlaggedTweetsPaginated(
            String userId,
            Integer deleteFlag,
            int page,
            int size,
            String sortDirection) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 20) size = 20;
        Sort sort = Sort.by(
                sortDirection != null && sortDirection.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                "createdAt"
        );
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Tweet> tweetPage = tweetRepository.findByUserIdAndDeleteFlagOrderByCreatedAtDesc(
                userId,
                deleteFlag,
                pageable
        );
        // Convert to DTOs
        return tweetPage.map(TweetDto::new);
    }

    public Page<TweetDto> getTweetsPaginated(
            String userId,
            int page,
            int size,
            String sortDirection) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 20) size = 20;
        Sort sort = Sort.by(
                sortDirection != null && sortDirection.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                "createdAt"
        );
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Tweet> tweetPage = tweetRepository.findByUserIdOrderByCreatedAtDesc(
                userId,
                pageable
        );
        // Convert to DTOs
        return tweetPage.map(TweetDto::new);
    }

    public ResponseEntity<?> deleteTweetById(String tweetId, String userId){
        if(!tweetRepository.existsById(tweetId)){
            throw new UserNotFoundException("Tweet not found");
        }
        tweetRepository.deleteByTweetIdAndUserId(tweetId, userId);
        return ResponseEntity.ok(new TweetResponse("Tweet deleted",LocalDateTime.now()));
    }

    public ResponseEntity<List<EvaluationCriteria>> getAllEvaluationCriteria(){
        return ResponseEntity.ok(evaluationCriteriaRepository.findAll());
    }

}

