package com.interviewPrep.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;
    
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;
    
    @Column(name = "question_type", length = 50)
    private String questionType;
    
    @Column(length = 20)
    private String difficulty;
    
    @Column(length = 100)
    private String topic;
    
    @Column(name = "expected_answer", columnDefinition = "TEXT")
    private String expectedAnswer;
    
    @Column(name = "key_points", columnDefinition = "JSON")
    private String keyPoints; // JSON array of key concepts to cover
    
    @Column(name = "order_number")
    private Integer orderNumber;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL)
    private Answer answer;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}