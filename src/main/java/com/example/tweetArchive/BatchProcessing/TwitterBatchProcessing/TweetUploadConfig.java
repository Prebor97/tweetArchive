package com.example.tweetArchive.BatchProcessing.TwitterBatchProcessing;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.example.tweetArchive.dto.twitterDto.TweetJson;
import com.example.tweetArchive.dto.twitterDto.TweetJsonWrapper;
import com.example.tweetArchive.entities.Tweet;
import com.example.tweetArchive.repository.TweetRepository;
import com.example.tweetArchive.repository.UserInfoRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.json.JacksonJsonObjectReader;
import org.springframework.batch.infrastructure.item.json.JsonItemReader;
import org.springframework.batch.infrastructure.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Configuration
@Slf4j
public class TweetUploadConfig {
    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public TweetUploadConfig(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    @Bean
    @StepScope
    public JsonItemReader<TweetJsonWrapper> tweetJsonReader(
            @Value("#{jobParameters['filePath']}") String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("filePath cannot be null");
        }
        log.info("Creating tweetJsonReader with filePath: {}", filePath);
        S3Object s3Object = s3Client.getObject(bucketName, filePath);
        InputStream inputStream = s3Object.getObjectContent();

        Resource resource = new InputStreamResource(inputStream);
        if (!resource.exists())
        {
            throw new IllegalArgumentException("File does not exist at the specified path: " + filePath);
        }
        return new JsonItemReaderBuilder<TweetJsonWrapper>()
                .name("tweetJsonReader")
                .resource(resource)
                .jsonObjectReader(new JacksonJsonObjectReader<>(TweetJsonWrapper.class))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<TweetJsonWrapper, Tweet> tweetProcessor(
            TweetRepository tweetRepository,
            UserInfoRepository userInfoRepository,
            @Value("#{jobParameters['username']}") String username,
            @Value("#{jobParameters['user_id']}") String user_id)
    {

        return wrapper -> {
            TweetJson json = wrapper.getTweet();
            String rawText = json.getFull_text();

            if (rawText != null && rawText.startsWith("RT @")) return null;

            LocalDateTime createdAt = ZonedDateTime
                    .parse(json.getCreated_at(), FORMATTER)
                    .toLocalDateTime();

            String tweetUrl = "https://x.com/" + username + "/status/" + json.getId_str();

            String cleanedTweet = rawText
                    .replaceAll("https?://\\S+", "")
                    .replaceAll("@\\S+", "")
                    .replaceAll("\\b(\\w)\\b(?:\\s+\\1\\b)+", "")
                    .replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "")
                    .trim()
                    .toLowerCase();

            if (cleanedTweet.isEmpty()) return null;

            return new Tweet(json.getId_str(), createdAt, tweetUrl, user_id, username, cleanedTweet, 0);
        };
    }

    @Bean
    public ItemWriter<Tweet> tweetWriter(EntityManagerFactory emf) {
        return new JpaItemWriterBuilder<Tweet>()
                .entityManagerFactory(emf)
                .build();
    }

    @Bean
    @SuppressWarnings("removal")
    public Step importTweetsStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 JsonItemReader<TweetJsonWrapper> tweetJsonReader,
                                 ItemProcessor<TweetJsonWrapper, Tweet> tweetProcessor,
                                 ItemWriter<Tweet> tweetWriter) {

        return new StepBuilder("importTweetsStep", jobRepository)
                .<TweetJsonWrapper, Tweet>chunk(500, transactionManager)
                .reader(tweetJsonReader)
                .processor(tweetProcessor)
                .writer(tweetWriter)
                .faultTolerant()
//                .retryLimit(3)
//                .retry(Exception.class)
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public Job importTweetsJob(JobRepository jobRepository,
                               Step importTweetsStep) {
        return new JobBuilder("importTweetsJob", jobRepository)
//                .incrementer(new RunIdIncrementer())
                .start(importTweetsStep)
                .build();
    }
}

