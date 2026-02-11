package com.example.tweetArchive.controller;

import com.example.tweetArchive.dto.request.DeletionCriteriaDto;
import com.example.tweetArchive.dto.response.ErrorResponse;
import com.example.tweetArchive.dto.response.TweetResponse;
import com.example.tweetArchive.entities.EvaluationCriteria;
import com.example.tweetArchive.entities.Tweet;
import com.example.tweetArchive.exception.FileNotFoundException;
import com.example.tweetArchive.jwt.JwtUtils;
import com.example.tweetArchive.repository.EvaluationCriteriaRepository;
import com.example.tweetArchive.service.TweetJobService;
import com.example.tweetArchive.service.TweetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("v1/api/tweets")
public class TwitterController {
    private final TweetService tweetService;
    private final JwtUtils jwtUtils;
    private final TweetJobService tweetJobService;
    private final EvaluationCriteriaRepository evaluationCriteriaRepository;
    private static final Logger log = LoggerFactory.getLogger(TwitterController.class);


    public TwitterController(TweetService tweetService, JwtUtils jwtUtils1, TweetJobService tweetJobService, EvaluationCriteriaRepository evaluationCriteriaRepository) {
        this.tweetService = tweetService;
        this.jwtUtils = jwtUtils1;
        this.tweetJobService = tweetJobService;
        this.evaluationCriteriaRepository = evaluationCriteriaRepository;
    }

    @PostMapping("/upload-job")
    public ResponseEntity<?> uploadTweetsByBatch(@RequestHeader(name = "Authorization", required = false) String authHeader)
                                                 throws Exception {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication token is required");
        }
        String jwtToken = authHeader.substring(7);
        List<String> userInfo = jwtUtils.getUserInfo(jwtToken);
        String tweeterName = userInfo.get(1);
        String user_id = userInfo.get(0);
        tweetJobService.startImportJobAsync(user_id,tweeterName);
        return ResponseEntity.ok(new TweetResponse("Processing", LocalDateTime.now()));
    }

    @PostMapping("analysis-job")
    public ResponseEntity<?> analyseTweetsByBatch(@Valid @RequestBody DeletionCriteriaDto criteriaDto,
                                                  @RequestHeader(name = "Authorization", required = false) String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication token is required");
        }
        String jwtToken = authHeader.substring(7);
        List<String> userInfo = jwtUtils.getUserInfo(jwtToken);
        String user_id = userInfo.get(0);
        EvaluationCriteria evaluationCriteria = new EvaluationCriteria();
        evaluationCriteria.setCriteriaName(criteriaDto.getCriteriaName());
        evaluationCriteria.setCriteriaList(criteriaDto.getCriteriaList());
        evaluationCriteria.setUserId(user_id);
        evaluationCriteria.setCreatedDate(LocalDateTime.now());
        EvaluationCriteria criteria = evaluationCriteriaRepository.save(evaluationCriteria);
        tweetJobService.startAnalysisJobAsync(user_id,criteria.getCriteriaList().toString());
        return ResponseEntity.ok(new TweetResponse("Processing", LocalDateTime.now()));
    }


    @PostMapping("/upload")
    public ResponseEntity<?> uploadTweets(@RequestParam("file") MultipartFile file,
                                          @RequestParam String tweeterName) {
        if (file.isEmpty()) {
            throw new FileNotFoundException("File not found");
        }
        try {
            tweetService.importTweets(file,tweeterName);
            return ResponseEntity.ok(new TweetResponse("Tweet information saved", LocalDateTime.now()));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new ErrorResponse(e.getMessage(),500,LocalDateTime.now()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(),500,LocalDateTime.now()));
        }
    }
}
