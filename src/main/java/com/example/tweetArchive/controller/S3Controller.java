package com.example.tweetArchive.controller;

import com.example.tweetArchive.jwt.JwtUtils;
import com.example.tweetArchive.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/s3")
public class S3Controller {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final S3Service s3Service;
    private final JwtUtils jwtUtils;

    public S3Controller(S3Service s3Service, JwtUtils jwtUtils) {
        this.s3Service = s3Service;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestHeader(name = "Authorization", required = false) String authHeader)
            throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok("Token not present");
        }
        String jwtToken = authHeader.substring(7);
        List<String> userInfo = jwtUtils.getUserInfo(jwtToken);
        String user_id = userInfo.get(0);
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String contentType = file.getContentType();
        long fileSize = file.getSize();
        InputStream inputStream = file.getInputStream();
        return s3Service.uploadFile(user_id, bucketName, fileName, fileSize, contentType, inputStream);
    }

    // Download a file from S3
//    @GetMapping("/download/{fileName}")
//    public String downloadFile(@PathVariable String fileName) {
//        return s3Service.downloadFile(fileName).getKey();
//    }
}
