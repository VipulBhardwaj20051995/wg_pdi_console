package com.wovengold.pdi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for PDISubmission to avoid lazy loading issues
 */
@Data
@NoArgsConstructor
public class PDISubmissionDTO {
    private Long id;
    private String customerName;
    private String state;
    private String customerEmail;
    private String customerPhone;
    private List<String> imageUrls;
    private String videoUrl;
    private LocalDateTime submissionDate;
    private boolean emailSent;
    private boolean smsSent;
} 