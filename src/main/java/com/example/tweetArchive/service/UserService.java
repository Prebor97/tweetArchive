package com.example.tweetArchive.service;

import com.example.tweetArchive.dto.request.LoginDto;
import com.example.tweetArchive.dto.request.SignupDto;
import com.example.tweetArchive.dto.response.UserResponse;
import com.example.tweetArchive.entities.UserInfo;
import com.example.tweetArchive.exception.PasswordMismatchException;
import com.example.tweetArchive.exception.UserAlreadyExistsException;
import com.example.tweetArchive.exception.UserNotFoundException;
import com.example.tweetArchive.jwt.JwtUtils;
import com.example.tweetArchive.repository.UserInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class UserService {
    private final UserInfoRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserInfoRepository repository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public ResponseEntity<UserResponse> register(SignupDto dto){
        if (repository.findByEmail(dto.getEmail()).isPresent())
            throw new UserAlreadyExistsException(repository.findByEmail(dto.getEmail()).get().getUserId());
        log.info("Saving user information.........................................................................");
        UserInfo user = saveUser(new UserInfo(), dto);
        log.info("User information with id {} has been saved ",user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponse(jwtUtils.generateToken(user),
                "User created", LocalDateTime.now()));
    }

    public ResponseEntity<UserResponse> login(LoginDto dto){
        if (repository.findByEmail(dto.getEmail()).isEmpty())
            throw new UserNotFoundException("User with mail does not exist");
        UserInfo userInfo = repository.findByEmail(dto.getEmail()).get();
        if(!passwordEncoder.matches(dto.getPassword(), userInfo.getPassword()))
            throw new PasswordMismatchException("Incorrect password");
        userInfo.setLastLoginAt(LocalDateTime.now());
        repository.save(userInfo);
        return ResponseEntity.status(HttpStatus.OK).body(new UserResponse(jwtUtils.generateToken(userInfo),
                "User logged in", LocalDateTime.now()));
    }

    private UserInfo saveUser(UserInfo user, SignupDto dto){
        if (!dto.getPassword().equals(dto.getConfirmPassword())){
            throw new PasswordMismatchException("Passwords do not match");
        }
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setIsActive(true);
        user.setTwitterUserName(dto.getTwitterUserName());
        user.setCreatedAt(LocalDateTime.now());
        return repository.save(user);
    }
}

