package com.interviewPrep.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "company_name", length = 100)
    private String companyName;
    
    @Column(name = "interview_type", length = 50)
    private String interviewType;
    
    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;
    
    @Column(name = "total_questions")
    private Integer totalQuestions;
    
    @Column(name = "answered_questions")
    @Builder.Default
    private Integer answeredQuestions = 0;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal score;
    
    @Column(name = "technical_score", precision = 5, scale = 2)
    private BigDecimal technicalScore;
    
    @Column(name = "communication_score", precision = 5, scale = 2)
    private BigDecimal communicationScore;
    
    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private InterviewStatus status = InterviewStatus.IN_PROGRESS;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
