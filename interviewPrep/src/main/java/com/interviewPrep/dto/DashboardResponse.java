package com.interviewPrep.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardResponse {
    private final long totalInterviews;
    private final BigDecimal averageScore;
    private final String latestResumeFeedback;
    private final Integer latestAtsScore;
}
