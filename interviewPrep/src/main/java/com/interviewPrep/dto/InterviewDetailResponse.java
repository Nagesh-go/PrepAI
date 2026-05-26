package com.interviewPrep.dto;

import com.interviewPrep.model.Interview;
import com.interviewPrep.model.Question;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class InterviewDetailResponse {
    private final Long id;
    private final Integer totalQuestions;
    private final Integer answeredQuestions;
    private final String status;
    private final BigDecimal score;
    private final List<QuestionDetailResponse> questions;

    public static InterviewDetailResponse from(Interview interview) {
        return InterviewDetailResponse.builder()
                .id(interview.getId())
                .totalQuestions(interview.getTotalQuestions())
                .answeredQuestions(interview.getAnsweredQuestions())
                .status(interview.getStatus() != null ? interview.getStatus().name() : null)
                .score(interview.getScore())
                .questions(
                        interview.getQuestions() == null
                                ? List.of()
                                : interview.getQuestions().stream().map(QuestionDetailResponse::from).toList()
                )
                .build();
    }

    @Getter
    @Builder
    public static class QuestionDetailResponse {
        private final Long id;
        private final String questionText;
        private final String questionType;
        private final String difficulty;
        private final String topic;
        private final Integer orderNumber;
        private final AnswerPresence answer;

        public static QuestionDetailResponse from(Question question) {
            return QuestionDetailResponse.builder()
                    .id(question.getId())
                    .questionText(question.getQuestionText())
                    .questionType(question.getQuestionType())
                    .difficulty(question.getDifficulty())
                    .topic(question.getTopic())
                    .orderNumber(question.getOrderNumber())
                    .answer(question.getAnswer() != null ? new AnswerPresence(question.getAnswer().getId()) : null)
                    .build();
        }
    }

    public record AnswerPresence(Long id) {}
}
