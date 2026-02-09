package com.example.tweetArchive.exception;

public class FileNotFoundException extends RuntimeException{
    public FileNotFoundException(String message){
        super(message);
    }
}
