package com.example.tweetArchive.dto.twitterDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UrlEntity {
    private String expanded_url;
}
