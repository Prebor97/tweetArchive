package com.example.tweetArchive.repository;

import com.example.tweetArchive.entities.EvaluationCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, String> {

}
