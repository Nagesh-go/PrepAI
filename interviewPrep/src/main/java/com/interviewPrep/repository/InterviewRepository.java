package com.interviewPrep.repository;

import com.interviewPrep.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
            SELECT DISTINCT i FROM Interview i
            LEFT JOIN FETCH i.questions q
            LEFT JOIN FETCH q.answer
            WHERE i.id = :id
            """)
    Optional<Interview> findByIdWithQuestions(@Param("id") Long id);
}
