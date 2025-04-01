package com.wovengold.pdi.service;

import com.wovengold.pdi.dto.PDISubmissionRequest;
import com.wovengold.pdi.model.PDISubmission;
import com.wovengold.pdi.repository.PDISubmissionRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PDIService {

    private final PDISubmissionRepository pdiSubmissionRepository;
    private final SmsService smsService;
    private JavaMailSender mailSender;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.notification.email:next.vipul2@gmail.com}")
    private String notificationEmail;

    @Value("${spring.mail.username:noreply@wovengold.com}")
    private String fromEmail;

    @Autowired
    public PDIService(PDISubmissionRepository pdiSubmissionRepository, SmsService smsService) {
        this.pdiSubmissionRepository = pdiSubmissionRepository;
        this.smsService = smsService;
    }

    @Autowired(required = false)
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Transactional
    public PDISubmission submitPDI(PDISubmissionRequest request) throws IOException, MessagingException {
        log.info("Processing PDI submission for customer: {}", request.getCustomerName());
        
        // Save submission first
        PDISubmission submission = saveSubmission(request);
        
        // Send SMS notification
        try {
            smsService.sendSms(submission);
            log.info("SMS sent successfully to customer: {}", submission.getCustomerPhone());
        } catch (Exception e) {
            log.error("Failed to send SMS to customer {}: {}", submission.getCustomerPhone(), e.getMessage());
        }
        
        return submission;
    }

    @Transactional
    public PDISubmission saveSubmission(PDISubmissionRequest request) throws IOException, MessagingException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save images
        List<String> imageUrls = new ArrayList<>();
        if (request.getImages() != null) {
            for (MultipartFile image : request.getImages()) {
                if (image != null && !image.isEmpty()) {
                    String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(image.getInputStream(), filePath);
                    imageUrls.add(fileName);
                    log.info("Saved image: {}", fileName);
                }
            }
        }

        // Save video
        String videoUrl = null;
        if (request.getVideo() != null && !request.getVideo().isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + request.getVideo().getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(request.getVideo().getInputStream(), filePath);
            videoUrl = fileName;
            log.info("Saved video: {}", fileName);
        }

        // Create PDI submission
        PDISubmission submission = new PDISubmission();
        submission.setCustomerName(request.getCustomerName());
        submission.setState(request.getState());
        submission.setModel(request.getModel());
        submission.setCustomerEmail(request.getCustomerEmail());
        submission.setCustomerPhone(request.getCustomerPhone());
        submission.setImageUrls(imageUrls);
        submission.setVideoUrl(videoUrl);

        log.info("Creating PDI submission for customer: {}", request.getCustomerName());
        
        // Save submission
        submission = pdiSubmissionRepository.save(submission);

        // Send email
        try {
            if (mailSender != null) {
                sendEmail(submission);
            } else {
                log.warn("Email service not available. Skipping email sending.");
                submission.setEmailSent(true);
                pdiSubmissionRepository.save(submission);
            }
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", submission.getCustomerEmail(), e.getMessage());
        }

        return submission;
    }

    @Transactional(readOnly = true)
    public Optional<PDISubmission> getPDISubmissionById(Long id) {
        log.info("Fetching PDI submission with ID: {}", id);
        Optional<PDISubmission> submissionOpt = pdiSubmissionRepository.findByIdWithImageUrls(id);
        if (submissionOpt.isPresent()) {
            PDISubmission submission = submissionOpt.get();
            // Force initialization of the collection within the transaction
            submission.getImageUrls().size();
        }
        return submissionOpt;
    }

    @Transactional(readOnly = true)
    public List<PDISubmission> getAllPDISubmissions() {
        log.info("Fetching all PDI submissions");
        List<PDISubmission> submissions = pdiSubmissionRepository.findAllWithImageUrls();
        // Force initialization of collections within the transaction
        submissions.forEach(submission -> submission.getImageUrls().size());
        return submissions;
    }

    private void sendEmail(PDISubmission submission) throws MessagingException {
        log.info("Sending confirmation email to: {}", submission.getCustomerEmail());
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(submission.getCustomerEmail());
        helper.addCc(notificationEmail);
        helper.setSubject("🚀 WovenGold PDI Submission Confirmation 🚀 - " + submission.getCustomerName() + ", " + submission.getState());
        
        // Read and process the HTML template
        String htmlContent;
        try {
            ClassPathResource resource = new ClassPathResource("templates/email-template.html");
            htmlContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read email template: {}", e.getMessage());
            throw new MessagingException("Failed to process email template", e);
        }
        
        // Replace placeholders with actual values
        htmlContent = htmlContent
            .replace("${customerName}", submission.getCustomerName())
            .replace("${state}", submission.getState())
            .replace("${model}", submission.getModel())
            .replace("${submissionDate}", submission.getSubmissionDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        
        helper.setText(htmlContent, true);

        // Add video attachment if available
        if (submission.getVideoUrl() != null) {
            Path videoPath = Paths.get(uploadDir, submission.getVideoUrl());
            try {
                helper.addAttachment("packing_video.mp4", videoPath.toFile());
            } catch (MessagingException e) {
                log.error("Failed to attach video file: {}", e.getMessage());
            }
        }

        // Add image attachments if available
        if (submission.getImageUrls() != null && !submission.getImageUrls().isEmpty()) {
            for (int i = 0; i < submission.getImageUrls().size(); i++) {
                String imageUrl = submission.getImageUrls().get(i);
                Path imagePath = Paths.get(uploadDir, imageUrl);
                try {
                    // Get file extension from original filename
                    String extension = imageUrl.substring(imageUrl.lastIndexOf('.'));
                    helper.addAttachment("packing_image_" + (i + 1) + extension, imagePath.toFile());
                } catch (MessagingException e) {
                    log.error("Failed to attach image file {}: {}", imageUrl, e.getMessage());
                }
            }
        }

        mailSender.send(message);
        submission.setEmailSent(true);
        pdiSubmissionRepository.save(submission);
        
        log.info("Email sent successfully to: {}", submission.getCustomerEmail());
    }
} 