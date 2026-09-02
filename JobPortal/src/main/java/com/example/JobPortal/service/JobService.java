package com.example.JobPortal.service;

import com.example.JobPortal.dto.JobRequest;
import com.example.JobPortal.dto.JobResponse;
import com.example.JobPortal.entity.Job;
import com.example.JobPortal.entity.Recruiter;
import com.example.JobPortal.entity.Skill;
import com.example.JobPortal.entity.User;
import com.example.JobPortal.repository.JobRepository;
import com.example.JobPortal.repository.RecruiterRepository;
import com.example.JobPortal.repository.SkillRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Transactional
    public JobResponse createJob(User user, JobRequest request) {
        Recruiter recruiter = recruiterRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Recruiter profile not found, please create your profile first"));

        Job job = new Job();
        job.setRecruiter(recruiter);
        applyRequestToJob(job, request);

        Job saved = jobRepository.save(job);
        return mapToResponse(saved);
    }

    @Transactional
    public JobResponse updateJob(User user, Long jobId, JobRequest request) {
        Recruiter recruiter = recruiterRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Recruiter profile not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new SecurityException("You are not allowed to modify this job posting");
        }

        applyRequestToJob(job, request);

        Job saved = jobRepository.save(job);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteJob(User user, Long jobId) {
        Recruiter recruiter = recruiterRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Recruiter profile not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new SecurityException("You are not allowed to delete this job posting");
        }

        try {
            jobRepository.delete(job);
            jobRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "This job cannot be deleted because it already has candidate applications. Remove or archive the applications first."
            );
        }
    }

    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return mapToResponse(job);
    }

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String normalizeForSearch(String value) {
        return value.toLowerCase().replace(" ", "");
    }

    private Expression<String> normalizedColumn(jakarta.persistence.criteria.CriteriaBuilder cb, Expression<String> column) {
        return cb.function("REPLACE", String.class, cb.lower(column), cb.literal(" "), cb.literal(""));
    }

    public List<JobResponse> searchJobs(String title, String location, String skill) {
        Specification<Job> spec = (root, query, cb) -> cb.conjunction();

        if (title != null && !title.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(normalizedColumn(cb, root.get("title")), "%" + normalizeForSearch(title) + "%"));
        }

        if (location != null && !location.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(normalizedColumn(cb, root.get("location")), "%" + normalizeForSearch(location) + "%"));
        }

        if (skill != null && !skill.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Job, Skill> skillJoin = root.join("skills");
                return cb.equal(normalizedColumn(cb, skillJoin.get("name")), normalizeForSearch(skill));
            });
        }

        return jobRepository.findAll(spec)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void applyRequestToJob(Job job, JobRequest request) {
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setExperienceRequired(request.getExperienceRequired());
        job.setApplicationDeadline(request.getApplicationDeadline());

        if (request.getSkills() != null) {
            Set<Skill> skillSet = new HashSet<>();
            for (String name : request.getSkills()) {
                Skill skill = skillRepository.findByName(name)
                        .orElseGet(() -> {
                            Skill newSkill = new Skill();
                            newSkill.setName(name);
                            return skillRepository.save(newSkill);
                        });
                skillSet.add(skill);
            }
            job.setSkills(skillSet);
        }
    }

    private JobResponse mapToResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setLocation(job.getLocation());
        response.setSalaryMin(job.getSalaryMin());
        response.setSalaryMax(job.getSalaryMax());
        response.setExperienceRequired(job.getExperienceRequired());
        response.setApplicationDeadline(job.getApplicationDeadline());
        response.setPostedAt(job.getPostedAt());
        response.setCompanyName(job.getRecruiter().getCompanyName());
        response.setRecruiterId(job.getRecruiter().getId());

        Set<String> skillNames = new HashSet<>();
        for (Skill skill : job.getSkills()) {
            skillNames.add(skill.getName());
        }
        response.setSkills(skillNames);

        return response;
    }
}