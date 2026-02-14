package com.example.tweetArchive.BatchProcessing.TwitterBatchProcessing;

import com.example.tweetArchive.entities.Tweet;
import com.example.tweetArchive.service.GrokAIService;
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
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.Map;


@Slf4j
@Configuration
public class TweetAnalysisConfig {


    @Bean
    @StepScope
    public JpaPagingItemReader<Tweet> tweetReader(
            EntityManagerFactory emf,
            @Value("#{jobParameters['user_id']}") String userId) {
        return new JpaPagingItemReaderBuilder<Tweet>()
                .name("tweetReader")
                .entityManagerFactory(emf)
                .queryString("SELECT t FROM Tweet t WHERE t.userId = :userId")
                .parameterValues(Map.of("userId", userId))
                .pageSize(250)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Tweet, Tweet> tweetAnalysisProcessor(
            GrokAIService grokAIService,
            @Value("#{jobParameters['criteria']}") String criteria) {
        return tweet -> {
            String response = grokAIService.ask(criteria, tweet);
            log.info("Tweet {} evaluated to: {}", tweet.getTweetId(), response);

            if ("true".equalsIgnoreCase(response.trim())) {
                tweet.setDeleteFlag(1);
            } else if ("false".equalsIgnoreCase(response.trim())) {
            } else {
                tweet.setDeleteFlag(2);
                log.warn("Unexpected Groq response for tweet {}: {}", tweet.getTweetId(), response);
            }
            return tweet;
        };
    }

    @Bean
    public ItemWriter<Tweet> tweetUpdateWriter(EntityManagerFactory emf) {
        return new JpaItemWriterBuilder<Tweet>()
                .entityManagerFactory(emf)
                .build();
    }

    @Bean
    @SuppressWarnings("removal")
    public Step analyzeTweetsStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  JpaPagingItemReader<Tweet> tweetReader,
                                  ItemProcessor<Tweet, Tweet> tweetAnalysisProcessor,
                                  ItemWriter<Tweet> tweetUpdateWriter,
                                  GroqRateLimitResetListener resetListener) {
        return new StepBuilder("analyzeTweetsStep", jobRepository)
                .<Tweet, Tweet>chunk(29, transactionManager)
                .reader(tweetReader)
                .processor(tweetAnalysisProcessor)
                .writer(tweetUpdateWriter)
                .listener(resetListener)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public Job analyzeTweetsJob(JobRepository jobRepository,
                                Step analyzeTweetsStep) {
        return new JobBuilder("analyzeTweetsJob", jobRepository)
                .start(analyzeTweetsStep)
                .build();
    }
}
