package com.wovengold.pdi.controller;

import com.wovengold.pdi.dto.PDISubmissionDTO;
import com.wovengold.pdi.dto.PDISubmissionRequest;
import com.wovengold.pdi.model.PDISubmission;
import com.wovengold.pdi.service.PDIService;

import jakarta.mail.MessagingException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = {"/api/pdi", "/api/sms"})
@Validated
@Slf4j
@CrossOrigin(origins = "*")
public class PDIController {

    private final PDIService pdiService;

    @Autowired
    public PDIController(PDIService pdiService) {
        this.pdiService = pdiService;
    }

    @PostMapping("/submit")
    public ResponseEntity<PDISubmission> submitPDI(
            @RequestParam("customerName") @NotBlank(message = "Customer name is required") String customerName,
            @RequestParam("state") @NotBlank(message = "State is required") String state,
            @RequestParam("model") @NotBlank(message = "Model is required") String model,
            @RequestParam("tubSerialNo")  String tubSerialNo,
            @RequestParam("customerEmail") @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String customerEmail,
            @RequestParam("customerPhone") @NotBlank(message = "Phone number is required") @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits") String customerPhone,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "video", required = false) MultipartFile video) throws IOException, MessagingException {
        
        log.info("Received PDI submission request for customer: {}", customerName);
        
        PDISubmissionRequest request = new PDISubmissionRequest();
        request.setCustomerName(customerName);
        request.setState(state);
        request.setModel(model);
        request.setTubSerialNo(tubSerialNo);
        request.setCustomerEmail(customerEmail);
        request.setCustomerPhone(customerPhone);
        request.setImages(images);
        request.setVideo(video);

        PDISubmission submission = pdiService.submitPDI(request);
        log.info("PDI submission completed successfully with ID: {}", submission.getId());
        
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/packeddata")
    public ResponseEntity<List<PDISubmission>> getPackedData(@RequestParam(value = "id", required = false) Long id) {
        log.info("Received request to fetch packed data with ID: {}", id != null ? id : "all");
        
        try {
            if (id != null) {
                Optional<PDISubmission> submissionOpt = pdiService.getPDISubmissionById(id);
                if (submissionOpt.isPresent()) {
                    PDISubmission submission = submissionOpt.get();
                    // Ensure collection is initialized
                    submission.getImageUrls().size();
                    return ResponseEntity.ok(List.of(submission));
                } else {
                    // If ID not found, return all submissions
                    log.info("ID {} not found, returning all submissions", id);
                    List<PDISubmission> submissions = pdiService.getAllPDISubmissions();
                    submissions.forEach(submission -> submission.getImageUrls().size());
                    return ResponseEntity.ok(submissions);
                }
            } else {
                // Return all submissions if no ID provided
                List<PDISubmission> submissions = pdiService.getAllPDISubmissions();
                submissions.forEach(submission -> submission.getImageUrls().size());
                return ResponseEntity.ok(submissions);
            }
        } catch (Exception e) {
            log.error("Error fetching packed data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/states")
    public ResponseEntity<List<String>> getIndianStates() {
        List<String> states = List.of(
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
            "Delhi", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jammu and Kashmir",
            "Jharkhand", "Karnataka", "Kerala", "Ladakh", "Lakshadweep", "Madhya Pradesh",
            "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha",
            "Puducherry", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana",
            "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal"
        );
        return ResponseEntity.ok(states);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleExceptions(Exception e) {
        log.error("Error in PDI controller: {}", e.getMessage(), e);
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 