package com.example.tweetArchive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class ErrorResponse {
    private String error;
    private int code;
    private LocalDateTime timestamp;
}