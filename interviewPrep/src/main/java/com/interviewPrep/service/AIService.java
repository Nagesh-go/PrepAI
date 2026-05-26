package com.interviewPrep.service;

import java.util.List;
import java.util.Map;

public interface AIService {
    
    // Resume Analysis
    Map<String, Object> analyzeResume(String resumeText);
    List<String> extractSkills(String resumeText);
    String generateResumeFeedback(String resumeText);
    int calculateATSScore(String resumeText);
    
    // Interview Question Generation
    List<String> generateInterviewQuestions(List<String> skills, String experienceLevel, String companyName, int count);
    String generateFollowUpQuestion(String originalQuestion, String userAnswer);
    
    // Answer Evaluation
    Map<String, Object> evaluateAnswer(String question, String userAnswer, String expectedAnswer);
    
    // Learning Roadmap
    String generateLearningRoadmap(List<String> weakAreas, String experienceLevel);
}