package com.example.tweetArchive.repository;

import com.example.tweetArchive.entities.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, String> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Tweet t WHERE t.cleanedTweet IS NULL OR t.cleanedTweet LIKE '%&gt;&gt;&gt;&gt;&gt;&gt;&gt;&gt;&gt;&gt;&gt;&gt;&gt;&gt;rasheed%'")
    int deleteInvalidTweets();
}
