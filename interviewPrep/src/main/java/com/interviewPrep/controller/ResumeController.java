package com.interviewPrep.controller;

import com.interviewPrep.dto.ResumeAnalysisResponse;
import com.interviewPrep.model.User;
import com.interviewPrep.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {
    
    private final ResumeService resumeService;
    
    @PostMapping("/upload")
    public ResponseEntity<ResumeAnalysisResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws Exception {
        Long userId = getUserId(authentication);
        ResumeAnalysisResponse response = resumeService.uploadAndAnalyzeResume(userId, file);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestResume(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(resumeService.getLatestResume(userId));
    }
    
    private Long getUserId(Authentication authentication) {
        // Extract user ID from JWT token
        return ((User) authentication.getPrincipal()).getId();
    }
}
