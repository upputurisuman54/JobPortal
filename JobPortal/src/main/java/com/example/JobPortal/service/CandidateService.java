package com.example.JobPortal.service;

import com.example.JobPortal.dto.CandidateProfileRequest;
import com.example.JobPortal.dto.CandidateProfileResponse;
import com.example.JobPortal.dto.CertificateDto;
import com.example.JobPortal.dto.InternshipDto;
import com.example.JobPortal.dto.ProjectDto;
import com.example.JobPortal.dto.ResumeUploadResponse;
import com.example.JobPortal.entity.Candidate;
import com.example.JobPortal.entity.Certificate;
import com.example.JobPortal.entity.Internship;
import com.example.JobPortal.entity.Project;
import com.example.JobPortal.entity.Skill;
import com.example.JobPortal.entity.User;
import com.example.JobPortal.repository.CandidateRepository;
import com.example.JobPortal.repository.SkillRepository;
import com.example.JobPortal.repository.UserRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${resume.upload.dir}")
    private String uploadDir;

    public CandidateProfileResponse getProfile(User user) {
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Candidate profile not found"));
        return mapToResponse(candidate);
    }

    @Transactional
    public CandidateProfileResponse createOrUpdateProfile(User user, CandidateProfileRequest request) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Candidate candidate = candidateRepository.findByUserId(managedUser.getId())
                .orElse(new Candidate());

        candidate.setUser(managedUser);
        candidate.setEducation(request.getEducation());
        candidate.setExperienceYears(request.getExperienceYears());
        candidate.setResumeUrl(request.getResumeUrl());
        candidate.setLinkedinUrl(request.getLinkedinUrl());
        candidate.setGithubUrl(request.getGithubUrl());
        candidate.setPortfolioUrl(request.getPortfolioUrl());

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
            candidate.setSkills(skillSet);
        }

        syncProjects(candidate, request.getProjects());
        syncInternships(candidate, request.getInternships());
        syncCertificates(candidate, request.getCertificates());

        Candidate saved = candidateRepository.save(candidate);
        return mapToResponse(saved);
    }

    private void syncProjects(Candidate candidate, List<ProjectDto> projectDtos) {
        if (projectDtos == null) {
            return;
        }
        candidate.getProjects().clear();
        for (ProjectDto dto : projectDtos) {
            Project project = new Project();
            project.setTitle(dto.getTitle());
            project.setDescription(dto.getDescription());
            project.setLink(dto.getLink());
            project.setCandidate(candidate);
            candidate.getProjects().add(project);
        }
    }

    private void syncInternships(Candidate candidate, List<InternshipDto> internshipDtos) {
        if (internshipDtos == null) {
            return;
        }
        candidate.getInternships().clear();
        for (InternshipDto dto : internshipDtos) {
            Internship internship = new Internship();
            internship.setCompanyName(dto.getCompanyName());
            internship.setRole(dto.getRole());
            internship.setDuration(dto.getDuration());
            internship.setDescription(dto.getDescription());
            internship.setCandidate(candidate);
            candidate.getInternships().add(internship);
        }
    }

    private void syncCertificates(Candidate candidate, List<CertificateDto> certificateDtos) {
        if (certificateDtos == null) {
            return;
        }
        candidate.getCertificates().clear();
        for (CertificateDto dto : certificateDtos) {
            Certificate certificate = new Certificate();
            certificate.setTitle(dto.getTitle());
            certificate.setIssuer(dto.getIssuer());
            certificate.setLink(dto.getLink());
            certificate.setCandidate(candidate);
            candidate.getCertificates().add(certificate);
        }
    }

    @Transactional
    public ResumeUploadResponse uploadResume(User user, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Candidate profile not found"));

        String extractedText;
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            extractedText = stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF file");
        }

        String storedFileName = "candidate_" + candidate.getId() + "_" + UUID.randomUUID() + ".pdf";

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path targetPath = uploadPath.resolve(storedFileName);
            file.transferTo(targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save resume file");
        }

        candidate.setResumeFileName(storedFileName);
        candidate.setResumeText(extractedText);
        candidateRepository.save(candidate);

        ResumeUploadResponse response = new ResumeUploadResponse();
        response.setResumeFileName(storedFileName);
        response.setExtractedText(extractedText);
        response.setMessage("Resume uploaded and text extracted successfully");
        return response;
    }

    public byte[] downloadResume(User user) {
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Candidate profile not found"));

        if (candidate.getResumeFileName() == null) {
            throw new RuntimeException("No resume has been uploaded yet");
        }

        Path filePath = Paths.get(uploadDir).resolve(candidate.getResumeFileName());

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resume file");
        }
    }

    public String getResumeFileName(User user) {
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Candidate profile not found"));

        if (candidate.getResumeFileName() == null) {
            throw new RuntimeException("No resume has been uploaded yet");
        }

        return candidate.getResumeFileName();
    }

    private CandidateProfileResponse mapToResponse(Candidate candidate) {
        CandidateProfileResponse response = new CandidateProfileResponse();
        response.setId(candidate.getId());
        response.setFullName(candidate.getUser().getFullName());
        response.setEmail(candidate.getUser().getEmail());
        response.setPhone(candidate.getUser().getPhone());
        response.setEducation(candidate.getEducation());
        response.setExperienceYears(candidate.getExperienceYears());
        response.setResumeUrl(candidate.getResumeUrl());
        response.setLinkedinUrl(candidate.getLinkedinUrl());
        response.setGithubUrl(candidate.getGithubUrl());
        response.setPortfolioUrl(candidate.getPortfolioUrl());

        Set<String> skillNames = new HashSet<>();
        for (Skill skill : candidate.getSkills()) {
            skillNames.add(skill.getName());
        }
        response.setSkills(skillNames);

        List<ProjectDto> projectDtos = new ArrayList<>();
        for (Project project : candidate.getProjects()) {
            ProjectDto dto = new ProjectDto();
            dto.setId(project.getId());
            dto.setTitle(project.getTitle());
            dto.setDescription(project.getDescription());
            dto.setLink(project.getLink());
            projectDtos.add(dto);
        }
        response.setProjects(projectDtos);

        List<InternshipDto> internshipDtos = new ArrayList<>();
        for (Internship internship : candidate.getInternships()) {
            InternshipDto dto = new InternshipDto();
            dto.setId(internship.getId());
            dto.setCompanyName(internship.getCompanyName());
            dto.setRole(internship.getRole());
            dto.setDuration(internship.getDuration());
            dto.setDescription(internship.getDescription());
            internshipDtos.add(dto);
        }
        response.setInternships(internshipDtos);

        List<CertificateDto> certificateDtos = new ArrayList<>();
        for (Certificate certificate : candidate.getCertificates()) {
            CertificateDto dto = new CertificateDto();
            dto.setId(certificate.getId());
            dto.setTitle(certificate.getTitle());
            dto.setIssuer(certificate.getIssuer());
            dto.setLink(certificate.getLink());
            certificateDtos.add(dto);
        }
        response.setCertificates(certificateDtos);

        return response;
    }
}