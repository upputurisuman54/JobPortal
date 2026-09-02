package com.example.JobPortal.service;

import com.example.JobPortal.dto.AiChatRequest;
import com.example.JobPortal.dto.AiChatResponse;
import com.example.JobPortal.entity.Application;
import com.example.JobPortal.entity.Candidate;
import com.example.JobPortal.entity.Job;
import com.example.JobPortal.entity.Skill;
import com.example.JobPortal.entity.User;
import com.example.JobPortal.repository.ApplicationRepository;
import com.example.JobPortal.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class AiChatService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private GeminiService geminiService;

    public AiChatResponse chat(User user, AiChatRequest request) {
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Candidate profile not found, please create your profile first"));

        String candidateSkills = candidate.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI assistant for a job portal, helping a job candidate. ");
        prompt.append("Respond only to what the candidate is asking. ");
        prompt.append("If asked to write a cover letter, write a complete one using the actual contact details given below, never use placeholders like [Your Phone Number], [Your Email], [Date], or [LinkedIn]. Use today's date: ").append(LocalDate.now()).append(". ");
        prompt.append("If asked to rewrite or improve their resume, produce a full rewritten resume in clean plain text, organized with clear section headings, using the actual details given below and never inserting placeholder text. ");
        prompt.append("Follow this exact structure and formatting when rewriting a resume: ");
        prompt.append("Line 1: the candidate's full name in capitals, alone on its own line. ");
        prompt.append("Line 2: phone | email | location, separated by the '|' character. ");
        prompt.append("Line 3: LinkedIn | GitHub | Portfolio, separated by '|', using the exact LinkedIn/GitHub/Portfolio URLs given in the candidate profile below, omitting any of the three that are blank or not provided. ");
        prompt.append("Leave one blank line, then write section headings in ALL CAPS alone on their own line, in exactly this order: PROFESSIONAL SUMMARY, SKILLS, PROJECTS, PROFESSIONAL EXPERIENCE, ACHIEVEMENTS, EDUCATION, CERTIFICATIONS. ");
        prompt.append("Under PROJECTS, each project starts with a single line combining the project title and its GitHub link like 'Project Title | GitHub: <link>' (omit the link portion entirely if none is known), optionally followed by a single 'Tech Stack: ...' line, then bullet points each starting with '- '. ");
        prompt.append("Under PROFESSIONAL EXPERIENCE, each entry starts with a single line combining role, company, and dates like 'Role | Company | Month Year - Month Year', followed by bullet points each starting with '- '. ");
        prompt.append("Under EDUCATION, each entry is a single line like 'Degree, Institution | CGPA: X | Year'. ");
        prompt.append("Under CERTIFICATIONS, list each as a bullet point like '- Certificate Name | Issuer | Month Year'. ");
        prompt.append("Under ACHIEVEMENTS, list each as a bullet point starting with '- '. ");
        prompt.append("Never combine an entry's title/role/degree line across multiple lines, never add markdown bold (**) anywhere, and never add extra blank lines between an entry's detail line and its bullets. ");
        prompt.append("If asked to make it ATS friendly, avoid tables, columns, graphics, and use standard section headings and simple formatting so applicant tracking systems can parse it correctly. ");
        prompt.append("If the candidate asks a general question about programming, technical concepts, career advice, or anything unrelated to resumes, answer it directly and concisely like a helpful assistant, without forcing it into resume or cover letter format. ");
        prompt.append("\n\nCandidate profile:\n");
        prompt.append("Name: ").append(candidate.getUser().getFullName()).append("\n");
        prompt.append("Email: ").append(candidate.getUser().getEmail()).append("\n");
        prompt.append("Phone: ").append(candidate.getUser().getPhone()).append("\n");
        prompt.append("Education: ").append(candidate.getEducation()).append("\n");
        prompt.append("Experience: ").append(candidate.getExperienceYears()).append(" years\n");
        prompt.append("Skills: ").append(candidateSkills).append("\n");
        prompt.append("LinkedIn: ").append(candidate.getLinkedinUrl() != null ? candidate.getLinkedinUrl() : "Not provided").append("\n");
        prompt.append("GitHub: ").append(candidate.getGithubUrl() != null ? candidate.getGithubUrl() : "Not provided").append("\n");
        prompt.append("Portfolio: ").append(candidate.getPortfolioUrl() != null ? candidate.getPortfolioUrl() : "Not provided").append("\n");

        String resumeContent = request.getResumeText();
        if (resumeContent == null || resumeContent.isBlank()) {
            resumeContent = candidate.getResumeText();
        }

        if (resumeContent != null && !resumeContent.isBlank()) {
            prompt.append("\nAdditional resume details provided by candidate:\n");
            prompt.append(resumeContent).append("\n");
        }

        if (request.getApplicationId() != null) {
            Application application = applicationRepository.findById(request.getApplicationId())
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            if (!application.getCandidate().getId().equals(candidate.getId())) {
                throw new SecurityException("You are not allowed to use this application for chat context");
            }

            Job job = application.getJob();
            prompt.append("\nTarget job title: ").append(job.getTitle()).append("\n");
            prompt.append("Target job description: ").append(job.getDescription()).append("\n");
            prompt.append("Target company: ").append(job.getRecruiter().getCompanyName()).append("\n");
        }

        prompt.append("\nCandidate message: ").append(request.getMessage());

        String reply = geminiService.generateContent(prompt.toString());

        AiChatResponse response = new AiChatResponse();
        response.setReply(reply);
        return response;
    }
}