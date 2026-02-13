package com.example.tweetArchive.controller;

import com.example.tweetArchive.dto.request.DeletionCriteriaDto;
import com.example.tweetArchive.dto.response.ErrorResponse;
import com.example.tweetArchive.dto.response.TweetResponse;
import com.example.tweetArchive.dto.twitterDto.TweetDto;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("v1/api/tweets")
@CrossOrigin(
        origins = "ec2-16-170-163-247.eu-north-1.compute.amazonaws.com:3000",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},
        allowedHeaders = "*",
        maxAge = 3600
)
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
        if (!evaluationCriteriaRepository.existsByCriteriaNameAndUserId(criteriaDto.getCriteriaName(), user_id)) {
            EvaluationCriteria evaluationCriteria = new EvaluationCriteria();
            evaluationCriteria.setCriteriaName(criteriaDto.getCriteriaName());
            evaluationCriteria.setCriteriaList(criteriaDto.getCriteriaList());
            evaluationCriteria.setUserId(user_id);
            evaluationCriteria.setCreatedDate(LocalDateTime.now());
            EvaluationCriteria criteria = evaluationCriteriaRepository.save(evaluationCriteria);
            tweetJobService.startAnalysisJobAsync(user_id, criteria.getCriteriaList().toString());
        }else {
            EvaluationCriteria criteria =
                    evaluationCriteriaRepository.findByCriteriaNameAndUserId(criteriaDto.getCriteriaName(), user_id)
                    .orElseThrow();
            tweetJobService.startAnalysisJobAsync(user_id, criteria.getCriteriaList().toString());
        }
        return ResponseEntity.ok(new TweetResponse("Processing", LocalDateTime.now()));
    }

    @GetMapping("/flagged")
    public ResponseEntity<Page<TweetDto>> getFlaggedTweets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        List<String> userInfo = jwtUtils.getUserInfo(token);
        String userId = userInfo.get(0);
            Page<TweetDto> result = tweetService.getFlaggedTweetsPaginated(
                    userId,
                    1,
                    page,
                    size,
                    sort
            );
            return ResponseEntity.ok(result);
        }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTweetById(@PathVariable String id,
                                             @RequestHeader(name = "Authorization", required = false) String authHeader ){
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        List<String> userInfo = jwtUtils.getUserInfo(token);
        String userId = userInfo.get(0);
        return tweetService.deleteTweetById(id,userId);
    }

    @GetMapping("/criteria")
    public ResponseEntity<List<EvaluationCriteria>> getAllCriteria(){
        return tweetService.getAllEvaluationCriteria();
    }

    @GetMapping()
    public ResponseEntity<Page<TweetDto>> getTweets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        List<String> userInfo = jwtUtils.getUserInfo(token);
        String userId = userInfo.get(0);

        Page<TweetDto> result = tweetService.getTweetsPaginated(
                userId,
                page,
                size,
                sort
        );
        return ResponseEntity.ok(result);
    }

}
