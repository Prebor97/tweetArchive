package com.example.tweetArchive.dto.twitterDto;

import com.example.tweetArchive.entities.Tweet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TweetDto {
    private String tweetId;
    private LocalDateTime createdAt;
    private String tweetUrl;
    private String userTwitterName;
    private String cleanedTweet;
    private Integer deleteFlag;
    private LocalDateTime updatedAt;

    public TweetDto(Tweet tweet) {
        this.tweetId         = tweet.getTweetId();
        this.createdAt       = tweet.getCreatedAt();
        this.tweetUrl        = tweet.getTweetUrl();
        this.userTwitterName = tweet.getUserTwitterName();
        this.cleanedTweet    = tweet.getCleanedTweet();
        this.deleteFlag      = tweet.getDeleteFlag();
        this.updatedAt       = tweet.getUpdatedAt();
    }
}
