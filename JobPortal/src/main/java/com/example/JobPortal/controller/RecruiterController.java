package com.example.JobPortal.controller;

import com.example.JobPortal.dto.RecruiterProfileRequest;
import com.example.JobPortal.dto.RecruiterProfileResponse;
import com.example.JobPortal.security.CustomUserDetails;
import com.example.JobPortal.service.RecruiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiter")
public class RecruiterController {

    @Autowired
    private RecruiterService recruiterService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            RecruiterProfileResponse response = recruiterService.getProfile(userDetails.getUser());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<RecruiterProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RecruiterProfileRequest request) {
        RecruiterProfileResponse response = recruiterService.createOrUpdateProfile(userDetails.getUser(), request);
        return ResponseEntity.ok(response);
    }
}