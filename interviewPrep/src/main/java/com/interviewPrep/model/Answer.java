package com.interviewPrep.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;
    
    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal score;
    
    @Column(name = "technical_accuracy", precision = 5, scale = 2)
    private BigDecimal technicalAccuracy;
    
    @Column(name = "completeness_score", precision = 5, scale = 2)
    private BigDecimal completenessScore;
    
    @Column(name = "clarity_score", precision = 5, scale = 2)
    private BigDecimal clarityScore;
    
    @Column(columnDefinition = "TEXT")
    private String strengths;
    
    @Column(columnDefinition = "TEXT")
    private String weaknesses;
    
    @Column(name = "improvement_suggestions", columnDefinition = "TEXT")
    private String improvementSuggestions;
    
    @Column(name = "answered_at", updatable = false)
    private LocalDateTime answeredAt;
    
    @PrePersist
    protected void onCreate() {
        answeredAt = LocalDateTime.now();
    }
}