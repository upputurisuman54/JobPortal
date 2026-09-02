package com.example.JobPortal.controller;

import com.example.JobPortal.dto.CandidateProfileRequest;
import com.example.JobPortal.dto.CandidateProfileResponse;
import com.example.JobPortal.dto.ResumeUploadResponse;
import com.example.JobPortal.security.CustomUserDetails;
import com.example.JobPortal.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidate")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            CandidateProfileResponse response = candidateService.getProfile(userDetails.getUser());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CandidateProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CandidateProfileRequest request) {
        CandidateProfileResponse response = candidateService.createOrUpdateProfile(userDetails.getUser(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resume/upload")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> uploadResume(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        try {
            ResumeUploadResponse response = candidateService.uploadResume(userDetails.getUser(), file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/resume/download")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> downloadResume(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            byte[] fileBytes = candidateService.downloadResume(userDetails.getUser());
            String filename = candidateService.getResumeFileName(userDetails.getUser());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

            return ResponseEntity.ok().headers(headers).body(fileBytes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}