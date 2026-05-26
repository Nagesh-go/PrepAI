package com.interviewPrep.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewPrep.dto.ResumeAnalysisResponse;
import com.interviewPrep.exception.ResourceNotFoundException;
import com.interviewPrep.model.Resume;
import com.interviewPrep.model.User;
import com.interviewPrep.repository.ResumeRepository;
import com.interviewPrep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final AIService aiService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ResumeAnalysisResponse uploadAndAnalyzeResume(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String resumeText = extractTextFromPDF(file);
        Map<String, Object> analysis = aiService.analyzeResume(resumeText);
        List<String> skills = toStringList(analysis.get("skills"));

        Resume resume = Resume.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .resumeText(resumeText)
                .extractedSkills(objectMapper.writeValueAsString(skills))
                .experienceLevel(String.valueOf(analysis.getOrDefault("experienceLevel", "Entry-Level")))
                .aiFeedback(generateFeedbackText(analysis))
                .atsScore(toInteger(analysis.get("atsScore")))
                .build();

        Resume savedResume = resumeRepository.save(resume);
        return toResponse(savedResume, skills);
    }

    @Transactional(readOnly = true)
    public ResumeAnalysisResponse getLatestResume(Long userId) {
        Resume resume = resumeRepository.findTopByUserIdOrderByUploadedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No resume found for user"));

        return toResponse(resume, parseSkills(resume.getExtractedSkills()));
    }

    private String extractTextFromPDF(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF resumes are supported");
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private ResumeAnalysisResponse toResponse(Resume resume, List<String> skills) {
        return ResumeAnalysisResponse.builder()
                .resumeId(resume.getId())
                .fileName(resume.getFileName())
                .extractedSkills(skills)
                .experienceLevel(resume.getExperienceLevel())
                .aiFeedback(resume.getAiFeedback())
                .atsScore(resume.getAtsScore())
                .build();
    }

    private String generateFeedbackText(Map<String, Object> analysis) {
        StringBuilder feedback = new StringBuilder();
        feedback.append("Resume Strengths:\n");
        toStringList(analysis.get("strengths")).forEach(strength ->
                feedback.append("- ").append(strength).append("\n"));

        feedback.append("\nAreas to Improve:\n");
        toStringList(analysis.get("improvements")).forEach(improvement ->
                feedback.append("- ").append(improvement).append("\n"));

        return feedback.toString();
    }

    private List<String> parseSkills(String skillsJson) {
        try {
            return objectMapper.readValue(
                    skillsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<String> toStringList(Object value) {
        if (value instanceof List<?> values) {
            return values.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            return 0;
        }
    }
}
