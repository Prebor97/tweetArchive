package com.example.tweetArchive.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "evaluation_criteria")
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class EvaluationCriteria {
    @Id
    @Column(name = "criteria_id", nullable = false, updatable = false)
    private String criteriaId;

    @Column(name = "criteria_name")
    private String criteriaName;

    @Column(name = "criteria_list")
    private List<String> criteriaList;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @PrePersist
    public void generateId() {
        if (this.criteriaId == null) {
            this.criteriaId = UUID.randomUUID().toString();
        }
    }
}
