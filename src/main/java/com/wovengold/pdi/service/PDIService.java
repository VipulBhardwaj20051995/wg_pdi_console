package com.wovengold.pdi.service;

import com.wovengold.pdi.dto.PDISubmissionRequest;
import com.wovengold.pdi.model.PDISubmission;
import com.wovengold.pdi.model.PDIImage;
import com.wovengold.pdi.repository.PDISubmissionRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.mail.MailException;

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
        
        // Send email asynchronously
        if (mailSender != null) {
            sendEmailAsync(submission);
        } else {
            log.warn("Email service not available. Skipping email sending.");
            submission.setEmailSent(true);
            pdiSubmissionRepository.save(submission);
        }
        
        return submission;
    }

    @Async
    protected void sendEmailAsync(PDISubmission submission) {
        try {
            sendEmail(submission);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", submission.getCustomerEmail(), e.getMessage());
        }
    }

    @Transactional
    public PDISubmission saveSubmission(PDISubmissionRequest request) throws IOException, MessagingException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory at: {}", uploadPath);
        }

        // Create PDI submission
        PDISubmission submission = new PDISubmission();
        submission.setCustomerName(request.getCustomerName());
        submission.setState(request.getState());
        submission.setModel(request.getModel());
        submission.setCustomerEmail(request.getCustomerEmail());
        submission.setCustomerPhone(request.getCustomerPhone());
        submission.setTubSerialNo(request.getTubSerialNo());

        // Save images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<PDIImage> images = new ArrayList<>();
            for (MultipartFile image : request.getImages()) {
                if (image != null && !image.isEmpty()) {
                    String originalFilename = image.getOriginalFilename();
                    String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                    String fileName = UUID.randomUUID().toString() + fileExtension;
                    
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    
                    PDIImage pdiImage = new PDIImage();
                    pdiImage.setPdiSubmission(submission);
                    pdiImage.setImageUrl(fileName);
                    images.add(pdiImage);
                    log.info("Saved image: {} to {}", originalFilename, filePath);
                }
            }
            submission.setImageUrls(images);
        }

        // Save video
        if (request.getVideo() != null && !request.getVideo().isEmpty()) {
            String originalFilename = request.getVideo().getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String fileName = UUID.randomUUID().toString() + fileExtension;
            
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(request.getVideo().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            submission.setVideoUrl(fileName);
            log.info("Saved video: {} to {}", originalFilename, filePath);
        }

        log.info("Creating PDI submission for customer: {}", request.getCustomerName());
        
        // Save submission
        submission = pdiSubmissionRepository.save(submission);
        return submission;
    }

    private void sendEmail(PDISubmission submission) throws MessagingException {
        log.info("Sending confirmation email via Gmail SMTP to: {}", submission.getCustomerEmail());
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(submission.getCustomerEmail());
            helper.addCc(notificationEmail);
            helper.setSubject("WovenGold PDI Submission Confirmation - " + submission.getCustomerName() + ", " + submission.getState());
            
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
                if (Files.exists(videoPath)) {
                    try {
                        helper.addAttachment("packing_video.mp4", videoPath.toFile());
                        log.info("Added video attachment: {}", videoPath);
                    } catch (MessagingException e) {
                        log.error("Failed to attach video file: {}", e.getMessage());
                    }
                } else {
                    log.warn("Video file not found at: {}", videoPath);
                }
            }

            // Add image attachments if available
            if (submission.getImageUrls() != null && !submission.getImageUrls().isEmpty()) {
                for (int i = 0; i < submission.getImageUrls().size(); i++) {
                    PDIImage pdiImage = submission.getImageUrls().get(i);
                    Path imagePath = Paths.get(uploadDir, pdiImage.getImageUrl());
                    if (Files.exists(imagePath)) {
                        try {
                            String extension = pdiImage.getImageUrl().substring(pdiImage.getImageUrl().lastIndexOf('.'));
                            helper.addAttachment("packing_image_" + (i + 1) + extension, imagePath.toFile());
                            log.info("Added image attachment: {}", imagePath);
                        } catch (MessagingException e) {
                            log.error("Failed to attach image file {}: {}", pdiImage.getImageUrl(), e.getMessage());
                        }
                    } else {
                        log.warn("Image file not found at: {}", imagePath);
                    }
                }
            }

            mailSender.send(message);
            submission.setEmailSent(true);
            pdiSubmissionRepository.save(submission);
            
            log.info("Email sent successfully via Gmail SMTP to: {}", submission.getCustomerEmail());
        } catch (Exception e) {
            log.error("Failed to send email via Gmail SMTP: {}", e.getMessage());
            throw new MessagingException("Failed to send email via Gmail SMTP", e);
        }
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
} 