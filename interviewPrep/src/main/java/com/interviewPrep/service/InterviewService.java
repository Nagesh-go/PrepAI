package com.interviewPrep.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewPrep.dto.AnswerSubmissionRequest;
import com.interviewPrep.dto.InterviewDetailResponse;
import com.interviewPrep.dto.InterviewRequest;
import com.interviewPrep.dto.InterviewResponse;
import com.interviewPrep.dto.InterviewSummaryResponse;
import com.interviewPrep.dto.QuestionResponse;
import com.interviewPrep.exception.UnauthorizedException;
import com.interviewPrep.exception.ResourceNotFoundException;
import com.interviewPrep.model.Answer;
import com.interviewPrep.model.Interview;
import com.interviewPrep.model.InterviewStatus;
import com.interviewPrep.model.Question;
import com.interviewPrep.model.Resume;
import com.interviewPrep.model.User;
import com.interviewPrep.repository.AnswerRepository;
import com.interviewPrep.repository.InterviewRepository;
import com.interviewPrep.repository.QuestionRepository;
import com.interviewPrep.repository.ResumeRepository;
import com.interviewPrep.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {
    
    private final InterviewRepository interviewRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final AIService aiService;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public InterviewResponse startInterview(Long userId, InterviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Get user's skills from latest resume
        Resume latestResume = resumeRepository.findTopByUserIdOrderByUploadedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Please upload a resume first"));
        
        List<String> skills = parseSkills(latestResume.getExtractedSkills());
        
        // Create interview
        Interview interview = Interview.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .interviewType(request.getInterviewType())
                .difficultyLevel(request.getDifficultyLevel())
                .totalQuestions(request.getQuestionCount())
                .answeredQuestions(0)
                .status(InterviewStatus.IN_PROGRESS)
                .build();
        
        interview = interviewRepository.save(interview);
        
        // Generate questions using AI
        List<String> questionTexts = aiService.generateInterviewQuestions(
                skills, 
                latestResume.getExperienceLevel(),
                request.getCompanyName(),
                request.getQuestionCount()
        );
        
        // Save questions
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < questionTexts.size(); i++) {
            Question question = Question.builder()
                    .interview(interview)
                    .questionText(questionTexts.get(i))
                    .questionType("TECHNICAL")
                    .difficulty(request.getDifficultyLevel())
                    .orderNumber(i + 1)
                    .build();
            questions.add(questionRepository.save(question));
        }
        
        interview.setQuestions(questions);
        
        // Return first question
        return InterviewResponse.builder()
                .interviewId(interview.getId())
                .currentQuestion(QuestionResponse.from(questions.get(0)))
                .totalQuestions(interview.getTotalQuestions())
                .answeredQuestions(0)
                .status(interview.getStatus().name())
                .build();
    }
    
    @Transactional
    public InterviewResponse submitAnswer(Long userId, Long interviewId, AnswerSubmissionRequest request) {
        Interview interview = getOwnedInterview(userId, interviewId);
        
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        if (!question.getInterview().getId().equals(interviewId)) {
            throw new IllegalArgumentException("Question does not belong to this interview");
        }
        
        // Evaluate answer using AI
        Map<String, Object> evaluation = aiService.evaluateAnswer(
                question.getQuestionText(),
                request.getUserAnswer(),
                question.getExpectedAnswer()
        );
        
        // Save answer with evaluation
        Answer answer = Answer.builder()
                .question(question)
                .userAnswer(request.getUserAnswer())
                .aiFeedback((String) evaluation.get("feedback"))
                .score(toScore(evaluation.get("overallScore")))
                .technicalAccuracy(toScore(evaluation.get("technicalAccuracy")))
                .completenessScore(toScore(evaluation.get("completeness")))
                .clarityScore(toScore(evaluation.get("clarity")))
                .strengths(String.join("; ", toStringList(evaluation.get("strengths"))))
                .weaknesses(String.join("; ", toStringList(evaluation.get("weaknesses"))))
                .improvementSuggestions(String.join("; ", toStringList(evaluation.get("improvements"))))
                .build();
        
        Answer savedAnswer = answerRepository.save(answer);
        question.setAnswer(savedAnswer);
        
        // Update interview progress
        interview.setAnsweredQuestions(interview.getAnsweredQuestions() + 1);
        
        // Check if interview is complete
        if (interview.getAnsweredQuestions().equals(interview.getTotalQuestions())) {
            completeInterview(interview);
            return InterviewResponse.builder()
                    .interviewId(interview.getId())
                    .status("COMPLETED")
                    .totalQuestions(interview.getTotalQuestions())
                    .answeredQuestions(interview.getAnsweredQuestions())
                    .finalScore(interview.getScore())
                    .build();
        }
        
        Interview refreshedInterview = interviewRepository.findByIdWithQuestions(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));

        Question nextQuestion = refreshedInterview.getQuestions().stream()
                .filter(q -> q.getAnswer() == null)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No more questions"));
        
        return InterviewResponse.builder()
                .interviewId(interview.getId())
                .currentQuestion(QuestionResponse.from(nextQuestion))
                .previousAnswerFeedback(savedAnswer.getAiFeedback())
                .previousAnswerScore(savedAnswer.getScore())
                .totalQuestions(interview.getTotalQuestions())
                .answeredQuestions(interview.getAnsweredQuestions())
                .status(interview.getStatus().name())
                .build();
    }
    
    private void completeInterview(Interview interview) {
        // Calculate overall scores
        List<Answer> answers = interview.getQuestions().stream()
                .map(Question::getAnswer)
                .filter(a -> a != null)
                .collect(Collectors.toList());

        if (answers.isEmpty()) {
            interview.setScore(BigDecimal.ZERO);
            interview.setTechnicalScore(BigDecimal.ZERO);
            interview.setCommunicationScore(BigDecimal.ZERO);
            interview.setConfidenceScore(BigDecimal.ZERO);
            interview.setStatus(InterviewStatus.COMPLETED);
            interview.setCompletedAt(LocalDateTime.now());
            interviewRepository.save(interview);
            return;
        }
        
        BigDecimal avgScore = answers.stream()
                .map(Answer::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(answers.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal avgTechnical = answers.stream()
                .map(Answer::getTechnicalAccuracy)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(answers.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal avgClarity = answers.stream()
                .map(Answer::getClarityScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(answers.size()), 2, RoundingMode.HALF_UP);
        
        interview.setScore(avgScore);
        interview.setTechnicalScore(avgTechnical);
        interview.setCommunicationScore(avgClarity);
        interview.setConfidenceScore(avgScore); // Simplified for now
        interview.setStatus(InterviewStatus.COMPLETED);
        interview.setCompletedAt(LocalDateTime.now());
        
        interviewRepository.save(interview);
    }
    
    private List<String> parseSkills(String skillsJson) {
        try {
            return objectMapper.readValue(skillsJson, List.class);
        } catch (Exception e) {
            log.error("Error parsing skills JSON", e);
            return List.of();
        }
    }
    
    @Transactional(readOnly = true)
    public InterviewDetailResponse getInterviewDetail(Long userId, Long interviewId) {
        Interview interview = getOwnedInterview(userId, interviewId);
        return InterviewDetailResponse.from(
                interviewRepository.findByIdWithQuestions(interview.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Interview not found"))
        );
    }
    
    @Transactional(readOnly = true)
    public List<InterviewSummaryResponse> getUserInterviews(Long userId) {
        return interviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(InterviewSummaryResponse::from)
                .toList();
    }

    private Interview getOwnedInterview(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
        if (!interview.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this interview");
        }
        return interview;
    }

    private BigDecimal toScore(Object value) {
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return BigDecimal.valueOf(Double.parseDouble(String.valueOf(value)));
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private List<String> toStringList(Object value) {
        if (value instanceof List<?> values) {
            return values.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
