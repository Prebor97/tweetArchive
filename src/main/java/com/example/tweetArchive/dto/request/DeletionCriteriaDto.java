package com.example.tweetArchive.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeletionCriteriaDto {
    @NotBlank(message = "Email is required")
    private String criteriaName;

    @NotNull(message = "Criteria list is required")
    @NotEmpty(message = "Criteria list cannot be empty")
    @Size(min = 1, message = "Criteria list must have at least one item")
    private List<String> criteriaList;
}
