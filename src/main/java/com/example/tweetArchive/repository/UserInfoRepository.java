package com.example.tweetArchive.repository;

import com.example.tweetArchive.entities.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    Optional<UserInfo> findByEmail(String email);

    @Query("SELECT u.userId FROM UserInfo u WHERE u.twitterUserName = :name")
    Optional<String> findUserIdByTweeterName(@Param("name") String name);
}
