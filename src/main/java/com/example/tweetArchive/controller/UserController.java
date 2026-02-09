package com.example.tweetArchive.controller;

import com.example.tweetArchive.dto.request.LoginDto;
import com.example.tweetArchive.dto.request.SignupDto;
import com.example.tweetArchive.dto.response.UserResponse;
import com.example.tweetArchive.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody SignupDto dto){
        return userService.register(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginDto dto){
        return userService.login(dto);
    }
}

