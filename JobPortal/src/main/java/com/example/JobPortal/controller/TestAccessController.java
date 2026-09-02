package com.example.JobPortal.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestAccessController {

    @GetMapping("/recruiter/test")
    @PreAuthorize("hasRole('RECRUITER')")
    public String recruiterOnly() {
        return "Recruiter access confirmed";
    }

    @GetMapping("/candidate/test")
    @PreAuthorize("hasRole('CANDIDATE')")
    public String candidateOnly() {
        return "Candidate access confirmed";
    }
}