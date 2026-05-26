package com.interviewPrep.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "resume_text", columnDefinition = "TEXT")
    private String resumeText;
    
    @Column(name = "extracted_skills", columnDefinition = "JSON")
    private String extractedSkills; // Store as JSON string
    
    @Column(name = "experience_level", length = 50)
    private String experienceLevel;
    
    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;
    
    @Column(name = "ats_score")
    private Integer atsScore;
    
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
