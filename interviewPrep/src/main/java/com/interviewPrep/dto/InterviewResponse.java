package com.interviewPrep.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class InterviewResponse {
    private final Long interviewId;
    private final QuestionResponse currentQuestion;
    private final String previousAnswerFeedback;
    private final BigDecimal previousAnswerScore;
    private final Integer totalQuestions;
    private final Integer answeredQuestions;
    private final String status;
    private final BigDecimal finalScore;
}
