package com.example.tweetArchive.controller;

import com.example.tweetArchive.dto.request.LoginDto;
import com.example.tweetArchive.dto.request.SignupDto;
import com.example.tweetArchive.dto.response.UserResponse;
import com.example.tweetArchive.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/auth")
@CrossOrigin(
        origins = "ec2-16-170-163-247.eu-north-1.compute.amazonaws.com:3000",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},
        allowedHeaders = "*",
        maxAge = 3600
)
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

