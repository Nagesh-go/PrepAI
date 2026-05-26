package com.interviewPrep.service;

import com.interviewPrep.dto.DashboardResponse;
import com.interviewPrep.model.Interview;
import com.interviewPrep.model.Resume;
import com.interviewPrep.repository.InterviewRepository;
import com.interviewPrep.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final InterviewRepository interviewRepository;
    private final ResumeRepository resumeRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        List<Interview> interviews = interviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Resume latestResume = resumeRepository.findTopByUserIdOrderByUploadedAtDesc(userId).orElse(null);

        List<BigDecimal> scores = interviews.stream()
                .map(Interview::getScore)
                .filter(Objects::nonNull)
                .toList();

        BigDecimal averageScore = scores.isEmpty()
                ? BigDecimal.ZERO
                : scores.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);

        return DashboardResponse.builder()
                .totalInterviews(interviews.size())
                .averageScore(averageScore)
                .latestResumeFeedback(latestResume != null ? latestResume.getAiFeedback() : null)
                .latestAtsScore(latestResume != null ? latestResume.getAtsScore() : null)
                .build();
    }
}
