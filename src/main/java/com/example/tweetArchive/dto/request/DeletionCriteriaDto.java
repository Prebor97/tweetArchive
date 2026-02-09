package com.example.tweetArchive.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeletionCriteriaDto {
    List<String> deletionCriteria;
}
