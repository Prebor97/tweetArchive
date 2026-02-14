package com.example.tweetArchive.repository;

import com.example.tweetArchive.entities.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, String> {
    Optional<Tweet> findFirstByUserId(String id);

    long countByUserIdAndDeleteFlag(String userId, Integer deleteFlag);

    List<Tweet> findByUserIdAndDeleteFlag(String userId, Integer deleteFlag);

    long countByUserId(String userId);

    List<Tweet> findByUserIdAndDeleteFlagOrderByCreatedAtDesc(String userId, Integer deleteFlag);

    boolean existsByUserIdAndDeleteFlag(String userId, Integer deleteFlag);

    Page<Tweet> findByUserIdAndDeleteFlagOrderByCreatedAtDesc(
            String userId,
            Integer deleteFlag,
            Pageable pageable
    );

    Page<Tweet> findByUserIdOrderByCreatedAtDesc(
            String userId,
            Pageable pageable
    );

    void deleteByTweetIdAndUserId(String tweetId, String userId);
}
