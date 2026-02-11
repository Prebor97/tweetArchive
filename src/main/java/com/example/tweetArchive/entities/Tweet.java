package com.example.tweetArchive.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tweets")
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class Tweet {
    @Id
    @Column(name = "tweet_id", length = 255)
    private String tweetId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "tweet_url", length = 255, nullable = false)
    private String tweetUrl;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_twitter_name", length = 50, nullable = false)
    private String userTwitterName;

    @Column(name = "cleaned_tweet", columnDefinition = "TEXT")
    private String cleanedTweet;

    @Column(name = "delete_flag", nullable = false)
    private Integer deleteFlag = 0;

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;


    public Tweet(String tweetId, LocalDateTime createdAt, String tweetUrl, String userId, String userTwitterName, String cleanedTweet, Integer deleteFlag) {
        this.tweetId = tweetId;
        this.createdAt = createdAt;
        this.tweetUrl = tweetUrl;
        this.userId = userId;
        this.userTwitterName = userTwitterName;
        this.cleanedTweet = cleanedTweet;
        this.deleteFlag = deleteFlag;
    }
}

