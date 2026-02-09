package com.example.tweetArchive.dto.twitterDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entities {
    private List<UrlEntity> urls;
}
