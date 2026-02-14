package com.example.tweetArchive.BatchProcessing.TwitterBatchProcessing;

import com.example.tweetArchive.service.GrokAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroqRateLimitResetListener implements StepExecutionListener {

    private final GrokAIService grokAIService;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        grokAIService.resetCounter();
        log.info("Reset Groq API rate limit counter before step {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }
}
