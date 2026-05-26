package com.interviewPrep.dto;

import com.interviewPrep.model.Interview;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class InterviewSummaryResponse {
    private final Long id;
    private final String companyName;
    private final String interviewType;
    private final String difficultyLevel;
    private final Integer totalQuestions;
    private final Integer answeredQuestions;
    private final BigDecimal score;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime completedAt;

    public static InterviewSummaryResponse from(Interview interview) {
        return InterviewSummaryResponse.builder()
                .id(interview.getId())
                .companyName(interview.getCompanyName())
                .interviewType(interview.getInterviewType())
                .difficultyLevel(interview.getDifficultyLevel())
                .totalQuestions(interview.getTotalQuestions())
                .answeredQuestions(interview.getAnsweredQuestions())
                .score(interview.getScore())
                .status(interview.getStatus() != null ? interview.getStatus().name() : null)
                .createdAt(interview.getCreatedAt())
                .completedAt(interview.getCompletedAt())
                .build();
    }
}
