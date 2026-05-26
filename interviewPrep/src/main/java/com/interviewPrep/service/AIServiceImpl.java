package com.interviewPrep.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${openai.api.key:}")
    private String apiKey;
    
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;
    
    @Value("${openai.model:gpt-4o-mini}")
    private String model;
    
    private String callOpenAI(String prompt, double temperature) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackResponse(prompt);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", "You are an expert technical interviewer and career coach."),
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", 2000);
        
        try {
            String response = webClientBuilder.build()
                .post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("AI service error: " + e.getMessage());
        }
    }

    private String fallbackResponse(String prompt) {
        if (prompt.contains("\"skills\"")) {
            return """
                    {
                      "skills": ["Java", "Spring Boot", "MySQL", "REST APIs"],
                      "experienceLevel": "Entry-Level",
                      "strengths": ["Clear technical foundation", "Backend project experience"],
                      "improvements": ["Add measurable achievements", "Include deployment details"],
                      "atsScore": 70
                    }
                    """;
        }
        if (prompt.contains("JSON array of strings")) {
            return """
                    [
                      "Explain how dependency injection works in Spring Boot.",
                      "How would you design the database schema for a mock interview platform?",
                      "What is JWT authentication and how do you secure REST APIs with it?",
                      "How would you integrate an LLM API into a backend service?",
                      "Explain how you would track weak technical areas from interview answers."
                    ]
                    """;
        }
        if (prompt.contains("\"overallScore\"")) {
            return """
                    {
                      "overallScore": 70,
                      "technicalAccuracy": 70,
                      "completeness": 65,
                      "clarity": 75,
                      "strengths": ["Answer is understandable", "Mentions relevant concepts"],
                      "weaknesses": ["Needs more depth", "Missing concrete examples"],
                      "improvements": ["Use a structured explanation", "Add one practical example"],
                      "feedback": "Good start. Improve by covering key concepts more completely and using an example from a project."
                    }
                    """;
        }
        return "Review core fundamentals daily, practice mock answers, and revise weak topics with small projects.";
    }
    
    @Override
    public Map<String, Object> analyzeResume(String resumeText) {
        String prompt = String.format("""
            Analyze this resume and provide:
            1. A list of technical skills (return as comma-separated values)
            2. Experience level (Junior/Mid-Level/Senior)
            3. Key strengths (3-5 points)
            4. Areas for improvement (3-5 points)
            5. ATS optimization score (0-100)
            
            Resume:
            %s
            
            Return the response in this exact JSON format:
            {
                "skills": ["skill1", "skill2", ...],
                "experienceLevel": "Mid-Level",
                "strengths": ["strength1", "strength2", ...],
                "improvements": ["improvement1", "improvement2", ...],
                "atsScore": 85
            }
            """, resumeText);
        
        String response = callOpenAI(prompt, 0.3);
        
        try {
            // Parse JSON response
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            log.error("Error parsing resume analysis", e);
            return Map.of("error", "Failed to analyze resume");
        }
    }
    
    @Override
    public List<String> extractSkills(String resumeText) {
        Map<String, Object> analysis = analyzeResume(resumeText);
        return (List<String>) analysis.getOrDefault("skills", List.of());
    }
    
    @Override
    public String generateResumeFeedback(String resumeText) {
        Map<String, Object> analysis = analyzeResume(resumeText);
        
        StringBuilder feedback = new StringBuilder();
        feedback.append("Resume Analysis:\n\n");
        feedback.append("Strengths:\n");
        ((List<String>) analysis.get("strengths")).forEach(s -> feedback.append("- ").append(s).append("\n"));
        feedback.append("\nAreas for Improvement:\n");
        ((List<String>) analysis.get("improvements")).forEach(i -> feedback.append("- ").append(i).append("\n"));
        
        return feedback.toString();
    }
    
    @Override
    public int calculateATSScore(String resumeText) {
        Map<String, Object> analysis = analyzeResume(resumeText);
        return (int) analysis.getOrDefault("atsScore", 0);
    }
    
    @Override
    public List<String> generateInterviewQuestions(List<String> skills, String experienceLevel, String companyName, int count) {
        String skillsStr = String.join(", ", skills);
        
        String prompt = String.format("""
            Generate %d technical interview questions for a %s level candidate with skills in: %s
            Company: %s
            
            Requirements:
            - Mix of conceptual, practical, and scenario-based questions
            - Appropriate difficulty for %s level
            - Focus on real-world problem solving
            - Include system design questions if Senior level
            
            Return as a JSON array of strings:
            ["question1", "question2", ...]
            """, count, experienceLevel, skillsStr, companyName, experienceLevel);
        
        String response = callOpenAI(prompt, 0.7);
        
        try {
            return objectMapper.readValue(response, List.class);
        } catch (Exception e) {
            log.error("Error parsing questions", e);
            return List.of("What are your technical strengths?");
        }
    }
    
    @Override
    public String generateFollowUpQuestion(String originalQuestion, String userAnswer) {
        String prompt = String.format("""
            Based on this interview exchange, generate a relevant follow-up question:
            
            Question: %s
            Answer: %s
            
            Generate a follow-up that:
            - Probes deeper into their understanding
            - Tests practical application
            - Reveals gaps in knowledge
            
            Return only the follow-up question.
            """, originalQuestion, userAnswer);
        
        return callOpenAI(prompt, 0.7);
    }
    
    @Override
    public Map<String, Object> evaluateAnswer(String question, String userAnswer, String expectedAnswer) {
        String prompt = String.format("""
            Evaluate this interview answer:
            
            Question: %s
            User's Answer: %s
            Expected Key Points: %s
            
            Provide evaluation in this JSON format:
            {
                "overallScore": 85,
                "technicalAccuracy": 90,
                "completeness": 80,
                "clarity": 85,
                "strengths": ["strength1", "strength2"],
                "weaknesses": ["weakness1", "weakness2"],
                "improvements": ["suggestion1", "suggestion2"],
                "feedback": "Detailed constructive feedback..."
            }
            
            Scoring: 0-100 scale
            """, question, userAnswer, expectedAnswer != null ? expectedAnswer : "N/A");
        
        String response = callOpenAI(prompt, 0.3);
        
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            log.error("Error parsing answer evaluation", e);
            return Map.of("error", "Failed to evaluate answer");
        }
    }
    
    @Override
    public String generateLearningRoadmap(List<String> weakAreas, String experienceLevel) {
        String weakAreasStr = String.join(", ", weakAreas);
        
        String prompt = String.format("""
            Create a personalized learning roadmap for a %s developer to improve in: %s
            
            Structure the roadmap with:
            1. Week-by-week learning plan (4 weeks)
            2. Specific topics to study each week
            3. Recommended resources (courses, books, practice sites)
            4. Daily time commitment
            5. Milestones and checkpoints
            
            Make it actionable and realistic.
            """, experienceLevel, weakAreasStr);
        
        return callOpenAI(prompt, 0.7);
    }
}
