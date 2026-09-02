package com.example.JobPortal.dto;

import java.util.List;

public class AutoApplyResponse {

    private int totalJobsConsidered;
    private int totalApplied;
    private List<ApplicationResponse> applications;

    public int getTotalJobsConsidered() {
        return totalJobsConsidered;
    }

    public void setTotalJobsConsidered(int totalJobsConsidered) {
        this.totalJobsConsidered = totalJobsConsidered;
    }

    public int getTotalApplied() {
        return totalApplied;
    }

    public void setTotalApplied(int totalApplied) {
        this.totalApplied = totalApplied;
    }

    public List<ApplicationResponse> getApplications() {
        return applications;
    }

    public void setApplications(List<ApplicationResponse> applications) {
        this.applications = applications;
    }
}