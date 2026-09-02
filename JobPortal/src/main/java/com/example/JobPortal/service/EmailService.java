package com.example.JobPortal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendApplicationReceivedEmail(String toEmail, String candidateName, String jobTitle, String companyName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Application Received - " + jobTitle);
        message.setText("Hi " + candidateName + ",\n\n"
                + "Your application for " + jobTitle + " at " + companyName + " has been received successfully.\n\n"
                + "We will notify you once there is an update on your application status.\n\n"
                + "Best regards,\nJob Portal Team");
        mailSender.send(message);
    }

    @Async
    public void sendStatusUpdateEmail(String toEmail, String candidateName, String jobTitle, String companyName, String newStatus) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Application Status Update - " + jobTitle);

        String statusMessage;
        switch (newStatus) {
            case "SHORTLISTED":
                statusMessage = "Congratulations! You have been shortlisted for this role.";
                break;
            case "HIRED":
                statusMessage = "Congratulations! You have been selected for this role.";
                break;
            case "REJECTED":
                statusMessage = "We appreciate your interest, but we will not be moving forward with your application at this time.";
                break;
            default:
                statusMessage = "Your application status has been updated to " + newStatus + ".";
        }

        message.setText("Hi " + candidateName + ",\n\n"
                + "Your application status for " + jobTitle + " at " + companyName + " has been updated.\n\n"
                + statusMessage + "\n\n"
                + "Best regards,\nJob Portal Team");
        mailSender.send(message);
    }

    @Async
    public void sendPasswordResetOtpEmail(String toEmail, String fullName, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Password Reset Code");
        message.setText("Hi " + fullName + ",\n\n"
                + "Your password reset code is: " + otp + "\n\n"
                + "This code will expire in 10 minutes. If you did not request a password reset, "
                + "you can safely ignore this email.\n\n"
                + "Best regards,\nJob Portal Team");
        mailSender.send(message);
    }
}