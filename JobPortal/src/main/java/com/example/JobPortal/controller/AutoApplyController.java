package com.example.JobPortal.controller;

import com.example.JobPortal.dto.AutoApplyRequest;
import com.example.JobPortal.dto.AutoApplyResponse;
import com.example.JobPortal.security.CustomUserDetails;
import com.example.JobPortal.service.AutoApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class AutoApplyController {

    @Autowired
    private AutoApplyService autoApplyService;

    @PostMapping("/auto-apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> autoApply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AutoApplyRequest request) {
        try {
            AutoApplyResponse response = autoApplyService.autoApply(userDetails.getUser(), request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}