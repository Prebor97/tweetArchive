package com.example.tweetArchive.service;

import com.example.tweetArchive.entities.UserInfo;
import com.example.tweetArchive.repository.UserInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
public class TweetImportService {

    private final JobOperator jobOperator;
    private final Job importTweetsJob;
    private final UserInfoRepository repository;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private static final Logger log = LoggerFactory.getLogger(TweetImportService.class);

    public TweetImportService(JobOperator jobOperator, @Qualifier("importTweetsJob") Job importTweetsJob, UserInfoRepository repository) {
        this.jobOperator = jobOperator;
        this.importTweetsJob = importTweetsJob;
        this.repository = repository;
    }

    @Async
    public void startImportJobAsync(String userId, String tweeterName) throws Exception {
        log.info("Fetching file Path: ");
        UserInfo userInfo = repository.findById(userId).orElseThrow();
        JobParameters params = new JobParametersBuilder()
                .addString("filePath", userInfo.getFileLocation())
                .addString("username", tweeterName)
                .addString("user_id", userId)
                .addLong("runId", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobOperator.start(importTweetsJob, params);
        while (jobExecution.isRunning()) {
            Thread.sleep(1000);
        }
        if (!jobExecution.getStepExecutions().isEmpty()) {
            StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
            long successfulItems = stepExecution.getWriteCount();
            log.info("Number of tweets successfully imported: {}", successfulItems);
        } else {
            log.warn("No step executions found for job.");
        }
        if (jobExecution.getStatus().isUnsuccessful()) {
            log.info("Number of tweets unsuccessfully imported: {}",jobExecution.getAllFailureExceptions());
            log.info("Time of error: {}",LocalDateTime.now());
        }

        // Optionally log or handle completion here (e.g., send email, update DB status)
        log.info("Async job started with execution ID: {}", jobExecution.getId());
    }
}
