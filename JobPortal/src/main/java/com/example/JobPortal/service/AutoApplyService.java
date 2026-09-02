package com.example.JobPortal.service;

import com.example.JobPortal.dto.ApplicationRequest;
import com.example.JobPortal.dto.ApplicationResponse;
import com.example.JobPortal.dto.AutoApplyRequest;
import com.example.JobPortal.dto.AutoApplyResponse;
import com.example.JobPortal.dto.JobResponse;
import com.example.JobPortal.entity.Candidate;
import com.example.JobPortal.entity.Skill;
import com.example.JobPortal.entity.User;
import com.example.JobPortal.repository.ApplicationRepository;
import com.example.JobPortal.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AutoApplyService {

    private static final int MINIMUM_MATCH_SCORE = 70;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    @Transactional
    public AutoApplyResponse autoApply(User user, AutoApplyRequest request) {
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Candidate profile not found, please create your profile first"));

        List<JobResponse> matchingJobs = jobService.searchJobs(
                request.getPreferredTitle(),
                request.getPreferredLocation(),
                null
        );

        Set<String> candidateSkills = candidate.getSkills().stream()
                .map(Skill::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<ApplicationResponse> appliedApplications = new ArrayList<>();

        for (JobResponse job : matchingJobs) {
            if (applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidate.getId())) {
                continue;
            }

            if (job.getExperienceRequired() != null) {
                Integer candidateExperience = candidate.getExperienceYears();
                if (candidateExperience == null || candidateExperience < job.getExperienceRequired()) {
                    continue;
                }
            }

            int matchScore = calculateScore(candidateSkills, job.getSkills());
            if (matchScore < MINIMUM_MATCH_SCORE) {
                continue;
            }

            ApplicationRequest applicationRequest = new ApplicationRequest();
            applicationRequest.setJobId(job.getId());
            applicationRequest.setResumeUrl(candidate.getResumeUrl());

            try {
                ApplicationResponse applied = applicationService.applyToJob(user, applicationRequest);
                appliedApplications.add(applied);
            } catch (IllegalStateException e) {
                continue;
            }
        }

        AutoApplyResponse response = new AutoApplyResponse();
        response.setTotalJobsConsidered(matchingJobs.size());
        response.setTotalApplied(appliedApplications.size());
        response.setApplications(appliedApplications);
        return response;
    }

    private int calculateScore(Set<String> candidateSkills, Set<String> jobSkills) {
        if (jobSkills == null || jobSkills.isEmpty()) {
            return 0;
        }

        Set<String> jobSkillsLower = jobSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> matched = new HashSet<>(candidateSkills);
        matched.retainAll(jobSkillsLower);

        return (int) Math.round((matched.size() * 100.0) / jobSkillsLower.size());
    }
}