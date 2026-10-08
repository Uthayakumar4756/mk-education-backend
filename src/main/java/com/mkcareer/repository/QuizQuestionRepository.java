package com.mkcareer.repository;

import com.mkcareer.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    @Query(value = "SELECT * FROM mock_questions ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<QuizQuestion> findRandomQuestions();
}
