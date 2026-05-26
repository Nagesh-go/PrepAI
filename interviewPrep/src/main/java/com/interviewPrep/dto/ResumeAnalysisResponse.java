package com.interviewPrep.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResumeAnalysisResponse {
    private final Long resumeId;
    private final String fileName;
    private final List<String> extractedSkills;
    private final String experienceLevel;
    private final String aiFeedback;
    private final Integer atsScore;
}
