package com.example.JobPortal.controller;

import com.example.JobPortal.dto.AiChatRequest;
import com.example.JobPortal.dto.AiChatResponse;
import com.example.JobPortal.dto.PdfDownloadRequest;
import com.example.JobPortal.security.CustomUserDetails;
import com.example.JobPortal.service.AiChatService;
import com.example.JobPortal.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-chat")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private PdfService pdfService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> chat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AiChatRequest request) {
        try {
            AiChatResponse response = aiChatService.chat(userDetails.getUser(), request);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/download-pdf")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> downloadPdf(@RequestBody PdfDownloadRequest request) {
        try {
            byte[] pdfBytes = pdfService.generatePdf(request.getTitle(), request.getContent());

            String filename = (request.getTitle() != null && !request.getTitle().isBlank()
                    ? request.getTitle().replaceAll("[^a-zA-Z0-9]", "_")
                    : "document") + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}