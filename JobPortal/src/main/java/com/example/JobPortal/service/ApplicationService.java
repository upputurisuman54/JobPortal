package com.example.JobPortal.service;

import com.example.JobPortal.dto.ApplicationRequest;
import com.example.JobPortal.dto.ApplicationResponse;
import com.example.JobPortal.entity.Application;
import com.example.JobPortal.entity.ApplicationStatus;
import com.example.JobPortal.entity.Candidate;
import com.example.JobPortal.entity.Job;
import com.example.JobPortal.entity.Recruiter;
import com.example.JobPortal.entity.Skill;
import com.example.JobPortal.entity.User;
import com.example.JobPortal.repository.ApplicationRepository;
import com.example.JobPortal.repository.CandidateRepository;
import com.example.JobPortal.repository.JobRepository;
import com.example.JobPortal.repository.RecruiterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private EmailService emailService;

    @Value("${resume.upload.dir}")
    private String uploadDir;

    @Transactional
    public ApplicationResponse applyToJob(User user, ApplicationRequest request) {
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Candidate profile not found, please create your profile first"));

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidate.getId())) {
            throw new IllegalStateException("You have already applied to this job");
        }

        Application application = new Application();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setStatus(ApplicationStatus.APPLIED);

        String resumeUrl = request.getResumeUrl() != null ? request.getResumeUrl() : candidate.getResumeUrl();
        application.setResumeUrl(resumeUrl);

        application.setMatchScore(calculateMatchScore(candidate, job));

        Application saved = applicationRepository.save(application);

        emailService.sendApplicationReceivedEmail(
                candidate.getUser().getEmail(),
                candidate.getUser().getFullName(),
                job.getTitle(),
                job.getRecruiter().getCompanyName()
        );

        return mapToResponse(saved);
    }

    public List<ApplicationResponse> getMyApplications(User user) {
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Candidate profile not found"));

        return applicationRepository.findByCandidateId(candidate.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsForJob(User user, Long jobId) {
        Recruiter recruiter = recruiterRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Recruiter profile not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new SecurityException("You are not allowed to view applications for this job");
        }

        return applicationRepository.findByJobId(jobId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(User user, Long applicationId, String statusValue) {
        Recruiter recruiter = recruiterRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Recruiter profile not found"));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new SecurityException("You are not allowed to update this application");
        }

        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(statusValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Allowed values: APPLIED, SHORTLISTED, REJECTED, HIRED");
        }

        validateTransition(application.getStatus(), newStatus);

        application.setStatus(newStatus);
        Application saved = applicationRepository.save(application);

        emailService.sendStatusUpdateEmail(
                application.getCandidate().getUser().getEmail(),
                application.getCandidate().getUser().getFullName(),
                application.getJob().getTitle(),
                application.getJob().getRecruiter().getCompanyName(),
                newStatus.name()
        );

        return mapToResponse(saved);
    }

    public byte[] downloadResumeForApplication(User user, Long applicationId) {
        Recruiter recruiter = recruiterRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Recruiter profile not found"));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new SecurityException("You are not allowed to view this candidate's resume");
        }

        Candidate candidate = application.getCandidate();
        if (candidate.getResumeFileName() == null) {
            throw new RuntimeException("This candidate has not uploaded a resume");
        }

        Path filePath = Paths.get(uploadDir).resolve(candidate.getResumeFileName());

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resume file");
        }
    }

    private void validateTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        if (currentStatus == ApplicationStatus.HIRED || currentStatus == ApplicationStatus.REJECTED) {
            throw new IllegalArgumentException("Cannot change status once it is " + currentStatus + ", this is a final state");
        }

        if (currentStatus == ApplicationStatus.APPLIED) {
            if (newStatus != ApplicationStatus.SHORTLISTED && newStatus != ApplicationStatus.REJECTED) {
                throw new IllegalArgumentException("From APPLIED, status can only move to SHORTLISTED or REJECTED");
            }
        }

        if (currentStatus == ApplicationStatus.SHORTLISTED) {
            if (newStatus != ApplicationStatus.HIRED && newStatus != ApplicationStatus.REJECTED) {
                throw new IllegalArgumentException("From SHORTLISTED, status can only move to HIRED or REJECTED");
            }
        }
    }

    public Integer calculateMatchScore(Candidate candidate, Job job) {
        Set<String> candidateSkills = candidate.getSkills().stream()
                .map(Skill::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> jobSkills = job.getSkills().stream()
                .map(Skill::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (jobSkills.isEmpty()) {
            return 0;
        }

        Set<String> matched = new HashSet<>(candidateSkills);
        matched.retainAll(jobSkills);

        return (int) Math.round((matched.size() * 100.0) / jobSkills.size());
    }

    private ApplicationResponse mapToResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setJobId(application.getJob().getId());
        response.setJobTitle(application.getJob().getTitle());
        response.setCompanyName(application.getJob().getRecruiter().getCompanyName());
        response.setCandidateId(application.getCandidate().getId());
        response.setCandidateName(application.getCandidate().getUser().getFullName());
        response.setCandidateEmail(application.getCandidate().getUser().getEmail());
        response.setStatus(application.getStatus().name());
        response.setResumeUrl(application.getResumeUrl());
        response.setMatchScore(application.getMatchScore());
        response.setAppliedAt(application.getAppliedAt());
        return response;
    }
}