package com.interviewPrep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerSubmissionRequest {
    @NotNull
    private Long questionId;

    @NotBlank
    private String userAnswer;
}
