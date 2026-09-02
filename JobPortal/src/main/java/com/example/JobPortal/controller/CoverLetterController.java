package com.example.JobPortal.controller;

import com.example.JobPortal.dto.CoverLetterRequest;
import com.example.JobPortal.dto.CoverLetterResponse;
import com.example.JobPortal.security.CustomUserDetails;
import com.example.JobPortal.service.CoverLetterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cover-letters")
public class CoverLetterController {

    @Autowired
    private CoverLetterService coverLetterService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> createOrUpdate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CoverLetterRequest request) {
        try {
            CoverLetterResponse response = coverLetterService.createOrUpdateCoverLetter(userDetails.getUser(), request);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/generate/{applicationId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> generate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long applicationId) {
        try {
            CoverLetterResponse response = coverLetterService.generateCoverLetter(userDetails.getUser(), applicationId);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<?> getByApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long applicationId) {
        try {
            CoverLetterResponse response = coverLetterService.getCoverLetter(userDetails.getUser(), applicationId);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}