package com.example.tweetArchive.exception;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String userId){
        super("User with ID: " + userId + " already exists");
    }
}
