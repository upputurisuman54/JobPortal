package com.example.JobPortal.service;

import com.example.JobPortal.dto.CoverLetterRequest;
import com.example.JobPortal.dto.CoverLetterResponse;
import com.example.JobPortal.entity.Application;
import com.example.JobPortal.entity.Candidate;
import com.example.JobPortal.entity.CoverLetter;
import com.example.JobPortal.entity.Job;
import com.example.JobPortal.entity.Skill;
import com.example.JobPortal.entity.User;
import com.example.JobPortal.repository.ApplicationRepository;
import com.example.JobPortal.repository.CandidateRepository;
import com.example.JobPortal.repository.CoverLetterRepository;
import com.example.JobPortal.repository.RecruiterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class CoverLetterService {

    @Autowired
    private CoverLetterRepository coverLetterRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private GeminiService geminiService;

    @Transactional
    public CoverLetterResponse createOrUpdateCoverLetter(User user, CoverLetterRequest request) {
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Candidate profile not found"));

        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new SecurityException("You are not allowed to add a cover letter for this application");
        }

        CoverLetter coverLetter = coverLetterRepository.findByApplicationId(application.getId())
                .orElse(new CoverLetter());

        coverLetter.setApplication(application);
        coverLetter.setContent(request.getContent());

        CoverLetter saved = coverLetterRepository.save(coverLetter);
        return mapToResponse(saved);
    }

    @Transactional
    public CoverLetterResponse generateCoverLetter(User user, Long applicationId) {
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Candidate profile not found"));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new SecurityException("You are not allowed to generate a cover letter for this application");
        }

        Job job = application.getJob();

        String candidateSkills = candidate.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));

        String prompt = "Write a professional, concise cover letter for a job application. "
                + "Candidate name: " + candidate.getUser().getFullName() + ". "
                + "Candidate education: " + candidate.getEducation() + ". "
                + "Candidate experience: " + candidate.getExperienceYears() + " years. "
                + "Candidate skills: " + candidateSkills + ". "
                + "Job title: " + job.getTitle() + ". "
                + "Job description: " + job.getDescription() + ". "
                + "Company name: " + job.getRecruiter().getCompanyName() + ". "
                + "Keep it under 250 words, professional tone, no placeholders like [Your Name], "
                + "use the actual candidate name and company name given above.";

        String generatedContent = geminiService.generateContent(prompt);

        CoverLetter coverLetter = coverLetterRepository.findByApplicationId(applicationId)
                .orElse(new CoverLetter());

        coverLetter.setApplication(application);
        coverLetter.setContent(generatedContent);

        CoverLetter saved = coverLetterRepository.save(coverLetter);
        return mapToResponse(saved);
    }

    public CoverLetterResponse getCoverLetter(User user, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        boolean isOwningCandidate = candidateRepository.findByUserId(user.getId())
                .map(candidate -> candidate.getId().equals(application.getCandidate().getId()))
                .orElse(false);

        boolean isOwningRecruiter = recruiterRepository.findByUserId(user.getId())
                .map(recruiter -> recruiter.getId().equals(application.getJob().getRecruiter().getId()))
                .orElse(false);

        if (!isOwningCandidate && !isOwningRecruiter) {
            throw new SecurityException("You are not allowed to view this cover letter");
        }

        CoverLetter coverLetter = coverLetterRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new RuntimeException("Cover letter not found for this application"));

        return mapToResponse(coverLetter);
    }

    private CoverLetterResponse mapToResponse(CoverLetter coverLetter) {
        CoverLetterResponse response = new CoverLetterResponse();
        response.setId(coverLetter.getId());
        response.setApplicationId(coverLetter.getApplication().getId());
        response.setContent(coverLetter.getContent());
        response.setGeneratedAt(coverLetter.getGeneratedAt());
        return response;
    }
}