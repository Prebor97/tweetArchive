package com.example.tweetArchive.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String name){
        super("User Not Found with name: " + name);
    }
}
