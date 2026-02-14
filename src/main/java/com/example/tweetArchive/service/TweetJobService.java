package com.example.tweetArchive.service;

import com.example.tweetArchive.entities.UserInfo;
import com.example.tweetArchive.repository.TweetRepository;
import com.example.tweetArchive.repository.UserInfoRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class TweetJobService {

    private final JobOperator jobOperator;

    private final Job importTweetsJob;

    private final Job analyzeTweetsJob;

    private final UserInfoRepository repository;

    private final TweetRepository tweetRepository;

    private final EmailService emailService;

    public TweetJobService(JobOperator jobOperator, @Qualifier("importTweetsJob") Job importTweetsJob, Job analyzeTweetsJob, UserInfoRepository repository, TweetRepository tweetRepository, EmailService emailService) {
        this.jobOperator = jobOperator;
        this.importTweetsJob = importTweetsJob;
        this.analyzeTweetsJob = analyzeTweetsJob;
        this.repository = repository;
        this.tweetRepository = tweetRepository;
        this.emailService = emailService;
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
        sendMail(jobExecution,"importJob",userInfo);
        log.info("Async import job completed with execution ID: {}", jobExecution.getId());
    }

    @Async
    public void startAnalysisJobAsync(String userId, String criteria) throws Exception {
        log.info("Starting tweet analysis for user: {}", userId);
        UserInfo userInfo = repository.findById(userId).orElseThrow();
        JobParameters params = new JobParametersBuilder()
                .addString("user_id", userId)
                .addString("criteria", criteria)
                .addLong("runId", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobOperator.start(analyzeTweetsJob, params);
        while (jobExecution.isRunning()) {
            Thread.sleep(1000);
        }
        sendMail(jobExecution, "tweetAnalysisJob",userInfo);
        log.info("Async analysis job completed with execution ID: {}", jobExecution.getId());
    }

    private void sendMail(JobExecution jobExecution, String jobName, UserInfo userInfo) throws MessagingException {
        if (!jobExecution.getStepExecutions().isEmpty()) {
            StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
            long processedItems = stepExecution.getWriteCount();
            log.info("Number of tweets analyzed: {}", processedItems);
            if ("tweetAnalysisJob".equals(jobName)) {
                long flaggedTweets = tweetRepository.countByUserIdAndDeleteFlag(userInfo.getUserId(), 1);
                long totalTweets = tweetRepository.countByUserId(userInfo.getUserId());
                if (processedItems == 0) {
                    log.info("Sending analysis failure mail to {}", userInfo.getEmail());
                    emailService.sendTweetAnalysisFeedback(userInfo.getEmail(),flaggedTweets, totalTweets, userInfo.getLastName(),"failure");
                } else {
                    log.info("Sending analysis confirmation mail to {}", userInfo.getEmail());
                    emailService.sendTweetAnalysisFeedback(userInfo.getEmail(), flaggedTweets, totalTweets, userInfo.getLastName(), "success");
                }
            } else if ("importJob".equals(jobName)) {
                if (processedItems == 0) {
                    log.info("Sending failure mail to {}", userInfo.getEmail());
                    emailService.sendTweetUploadFeedback(userInfo.getEmail(), userInfo.getLastName(), "failure");
                } else {
                    log.info("Sending confirmation mail to {}", userInfo.getEmail());
                    emailService.sendTweetUploadFeedback(userInfo.getEmail(), userInfo.getLastName(),"success");
                }
            }
            if (jobExecution.getStatus().isUnsuccessful()) {
                log.error("Analysis job failed: {}", jobExecution.getAllFailureExceptions());
            }
        }
    }
}
