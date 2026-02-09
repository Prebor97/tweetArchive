package com.example.tweetArchive.dto.twitterDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TweetJson {
    private String id_str;
    private String created_at;
    private String full_text;
    private Entities entities;
}
