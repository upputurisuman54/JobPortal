package com.example.JobPortal.repository;

import com.example.JobPortal.entity.CoverLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {
    Optional<CoverLetter> findByApplicationId(Long applicationId);
}