package com.example.tweetArchive.controller;

import com.example.tweetArchive.dto.response.ErrorResponse;
import com.example.tweetArchive.dto.response.TweetResponse;
import com.example.tweetArchive.exception.FileNotFoundException;
import com.example.tweetArchive.jwt.JwtUtils;
import com.example.tweetArchive.service.TweetImportService;
import com.example.tweetArchive.service.TweetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("v1/api/tweets")
public class TwitterController {
    private final TweetService tweetService;
    private final JwtUtils jwtUtils;
    private final TweetImportService tweetImportService;
    private static final Logger log = LoggerFactory.getLogger(TwitterController.class);


    public TwitterController(TweetService tweetService, JwtUtils jwtUtils1, TweetImportService tweetImportService) {
        this.tweetService = tweetService;
        this.jwtUtils = jwtUtils1;
        this.tweetImportService = tweetImportService;
    }

    @PostMapping("/upload-job")
    public ResponseEntity<?> uploadTweetsByBatch(@RequestHeader(name = "Authorization", required = false) String authHeader)
                                                 throws Exception {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok("Token not present");
        }
        String jwtToken = authHeader.substring(7);
        List<String> userInfo = jwtUtils.getUserInfo(jwtToken);
        String tweeterName = userInfo.get(1);
        String user_id = userInfo.get(0);

//        Path tempFile = Files.createTempFile("tweets", ".json");
//        if (!tempFile.toFile().exists()) {    throw new IllegalStateException("Temporary file was not created.");}
//        file.transferTo(tempFile);
        tweetImportService.startImportJobAsync(user_id,tweeterName);
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
