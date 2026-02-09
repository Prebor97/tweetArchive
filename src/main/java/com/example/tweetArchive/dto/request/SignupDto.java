package com.example.tweetArchive.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class SignupDto {
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @JsonProperty("confirm_password")
    @NotBlank
    private String confirmPassword;

    @NotBlank(message = "FirstName is required")
    @Size(min = 3, max = 20, message = "FirstName must be between 3 and 20 characters")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "LastName is required")
    @Size(min = 3, max = 20, message = "LastName must be between 3 and 20 characters")
    @JsonProperty("last_name")
    private String lastName;

    @NotBlank(message = "Twitter user name is required")
    @JsonProperty("twitter_username")
    private String twitterUserName;
}

