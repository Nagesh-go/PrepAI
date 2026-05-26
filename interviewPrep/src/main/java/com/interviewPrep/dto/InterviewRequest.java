package com.interviewPrep.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterviewRequest {
    @NotBlank
    private String companyName;

    @NotBlank
    private String interviewType;

    @NotBlank
    private String difficultyLevel;

    @Min(1)
    @Max(20)
    private int questionCount = 5;
}
