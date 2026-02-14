package com.example.tweetArchive.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.tweetArchive.entities.UserInfo;
import com.example.tweetArchive.repository.UserInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.time.LocalDateTime;

@Slf4j
@Service
public class S3Service {

    private final AmazonS3 s3Client;
    private final UserInfoRepository repository;

    @Value("${cloud.aws.region.static}")
    private String region;

    public S3Service(AmazonS3 s3Client, UserInfoRepository repository) {
        this.s3Client = s3Client;
        this.repository = repository;
    }

    public ResponseEntity<?> uploadFile(String userId, String bucketName, String keyName, Long contentLength, String contentType, InputStream value
    ) throws AmazonClientException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(contentType);

        boolean exists = s3Client.doesObjectExist(bucketName, keyName);
        if (exists) {
            log.info("Removing existing file from bucket............................................................");
            s3Client.deleteObject(bucketName, keyName);
            log.info("Deleted existing object: bucket={}, key={}", bucketName, keyName);
        }

        s3Client.putObject(bucketName, keyName, value, metadata);
        UserInfo userInfo = repository.findById(userId).orElseThrow();
        userInfo.setFileLocation(keyName);
        userInfo.setUpdatedAt(LocalDateTime.now());
        repository.save(userInfo);

        log.info("Successfully uploaded object: bucket={}, key={}", bucketName, keyName);

        String message = exists
                ? "Existing file replaced and new file uploaded: " + keyName
                : "File uploaded successfully: " + keyName;
        return ResponseEntity.ok(message);
    }

    // Download file from S3 bucket
//    public S3Object downloadFile(String fileName) {
//        return amazonS3.getObject(bucketName, fileName);
//    }
}
