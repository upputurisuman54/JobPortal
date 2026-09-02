package com.example.JobPortal.service;

import com.example.JobPortal.dto.RecruiterProfileRequest;
import com.example.JobPortal.dto.RecruiterProfileResponse;
import com.example.JobPortal.entity.Recruiter;
import com.example.JobPortal.entity.User;
import com.example.JobPortal.repository.RecruiterRepository;
import com.example.JobPortal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecruiterService {

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private UserRepository userRepository;

    public RecruiterProfileResponse getProfile(User user) {
        Recruiter recruiter = recruiterRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Recruiter profile not found"));
        return mapToResponse(recruiter);
    }

    @Transactional
    public RecruiterProfileResponse createOrUpdateProfile(User user, RecruiterProfileRequest request) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Recruiter recruiter = recruiterRepository.findByUserId(managedUser.getId())
                .orElse(new Recruiter());

        recruiter.setUser(managedUser);
        recruiter.setCompanyName(request.getCompanyName());
        recruiter.setCompanyDescription(request.getCompanyDescription());

        Recruiter saved = recruiterRepository.save(recruiter);
        return mapToResponse(saved);
    }

    private RecruiterProfileResponse mapToResponse(Recruiter recruiter) {
        RecruiterProfileResponse response = new RecruiterProfileResponse();
        response.setId(recruiter.getId());
        response.setFullName(recruiter.getUser().getFullName());
        response.setEmail(recruiter.getUser().getEmail());
        response.setPhone(recruiter.getUser().getPhone());
        response.setCompanyName(recruiter.getCompanyName());
        response.setCompanyDescription(recruiter.getCompanyDescription());
        return response;
    }
}