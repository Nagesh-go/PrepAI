package com.interviewPrep.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.interviewPrep.dto.AnswerSubmissionRequest;
import com.interviewPrep.dto.InterviewDetailResponse;
import com.interviewPrep.dto.InterviewRequest;
import com.interviewPrep.dto.InterviewResponse;
import com.interviewPrep.dto.InterviewSummaryResponse;
import com.interviewPrep.model.User;
import com.interviewPrep.service.InterviewService;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {
    
    private final InterviewService interviewService;
    
    @PostMapping("/start")
    public ResponseEntity<InterviewResponse> startInterview(
            @Valid @RequestBody InterviewRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(interviewService.startInterview(userId, request));
    }
    
    @PostMapping("/{interviewId}/answer")
    public ResponseEntity<InterviewResponse> submitAnswer(
            @PathVariable Long interviewId,
            @Valid @RequestBody AnswerSubmissionRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(interviewService.submitAnswer(userId, interviewId, request));
    }
    
    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewDetailResponse> getInterview(
            @PathVariable Long interviewId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(interviewService.getInterviewDetail(userId, interviewId));
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<InterviewSummaryResponse>> getInterviewHistory(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(interviewService.getUserInterviews(userId));
    }
    
    private Long getUserId(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }
}