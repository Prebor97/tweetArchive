package com.example.tweetArchive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TweetArchiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(TweetArchiveApplication.class, args);
	}

}
