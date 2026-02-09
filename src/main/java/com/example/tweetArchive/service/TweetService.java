package com.example.tweetArchive.service;

import com.example.tweetArchive.dto.twitterDto.TweetJson;
import com.example.tweetArchive.dto.twitterDto.TweetJsonWrapper;
import com.example.tweetArchive.entities.Tweet;
import com.example.tweetArchive.exception.UserNotFoundException;
import com.example.tweetArchive.repository.TweetRepository;
import com.example.tweetArchive.repository.UserInfoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Transactional
public class TweetService {
    private final TweetRepository tweetRepository;
    private final UserInfoRepository userInfoRepository;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(TweetService.class);

    public TweetService(TweetRepository tweetRepository, UserInfoRepository userInfoRepository, ObjectMapper objectMapper) {
        this.tweetRepository = tweetRepository;
        this.userInfoRepository = userInfoRepository;
        this.objectMapper = objectMapper;
    }
    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);

    public void importTweets(MultipartFile file, String userName) throws IOException {
        if (userInfoRepository.findUserIdByTweeterName(userName).isEmpty())
            throw new UserNotFoundException(userName);
        String userId = userInfoRepository.findUserIdByTweeterName(userName).get();
        log.info("Read multipart file...........................................................................");
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        int index = content.indexOf('=');
        if (index == -1) {
            throw new IllegalArgumentException("Invalid Twitter export format");
        }

        String json = content.substring(index + 1).trim();
        if (json.endsWith(";")) {
            json = json.substring(0, json.length() - 1);
        }

        JsonNode root = objectMapper.readTree(json);
        log.info("Processing tweet.json file");

        for (JsonNode node : root) {
            saveSingleTweet(node, userId,userName);
        }
        log.info("Successfully saved tweets to database");
    }

    private void saveSingleTweet(JsonNode node, String userId, String username) throws IOException {
        TweetJsonWrapper wrapper = objectMapper.treeToValue(node, TweetJsonWrapper.class);
        TweetJson json = wrapper.getTweet();
        String rawText = json.getFull_text();

        if (rawText != null && rawText.startsWith("RT @")) {
            log.info("Skipping retweet: {}..................................................................", json.getId_str());
            return;
        }

        if (tweetRepository.existsById(json.getId_str())) {
            log.info("Tweet {} already exists, skipping", json.getId_str());
            return;
        }

        LocalDateTime createdAt = ZonedDateTime
                .parse(json.getCreated_at(), FORMATTER)
                .toLocalDateTime();

        String tweetUrl = "https://x.com/" + username + "/status/" + json.getId_str();

        String cleanedTweet = rawText
                .replaceAll("https?://\\S+", "")
                .replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "")
                .trim()
                .toLowerCase();

        log.info("Saving tweet with id {} to database.........................................", json.getId_str());

        Tweet tweet = new Tweet(
                json.getId_str(),
                createdAt,
                tweetUrl,
                userId,
                username,
                cleanedTweet,
                0
        );
        tweetRepository.save(tweet);
    }
}

