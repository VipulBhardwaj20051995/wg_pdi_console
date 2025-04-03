package com.wovengold.pdi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PDISubmissionRequest {
    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String customerPhone;

    private String tubSerialNo;

    private List<MultipartFile> images;
    private MultipartFile video;
}