package com.interviewPrep.dto;

import com.interviewPrep.model.Question;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionResponse {
    private final Long id;
    private final String questionText;
    private final String questionType;
    private final String difficulty;
    private final String topic;
    private final Integer orderNumber;

    public static QuestionResponse from(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .difficulty(question.getDifficulty())
                .topic(question.getTopic())
                .orderNumber(question.getOrderNumber())
                .build();
    }
}
