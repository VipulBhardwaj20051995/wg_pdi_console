package com.wovengold.pdi.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "pdi_submissions")
public class PDISubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String customerPhone;

    @ElementCollection
    @CollectionTable(name = "pdi_images")
    private List<String> imageUrls;

    @Column
    private String videoUrl;

    @Column(nullable = false)
    private LocalDateTime submissionDate;

    @Column
    private boolean emailSent;

    @Column
    private boolean smsSent;

    @PrePersist
    protected void onCreate() {
        submissionDate = LocalDateTime.now();
    }
} 