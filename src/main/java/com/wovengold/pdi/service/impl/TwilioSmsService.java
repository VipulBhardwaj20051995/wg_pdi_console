package com.wovengold.pdi.service.impl;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.wovengold.pdi.model.PDISubmission;
import com.wovengold.pdi.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@Primary
public class TwilioSmsService implements SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromNumber;

    @Override
    public void sendSms(PDISubmission submission) {
        try {
            // Initialize Twilio
            Twilio.init(accountSid, authToken);

            // Check if any of the required configurations are missing
            if (StringUtils.isEmpty(accountSid) || StringUtils.isEmpty(authToken) || StringUtils.isEmpty(fromNumber)) {
                log.warn("Twilio configuration is incomplete. SMS will not be sent.");
                submission.setSmsSent(false);
                return;
            }

            String phoneNumber = formatPhoneNumber(submission.getCustomerPhone());
            
            // Check if phone number is valid
            if (!isValidPhoneNumber(phoneNumber)) {
                log.warn("Invalid phone number format: {}. SMS will not be sent.", phoneNumber);
                submission.setSmsSent(false);
                return;
            }

            try {
                String messageBody = String.format(
                    "Hi %s from %s! Welcome to Wovengold! Your model %s will be packed today. The packing Video has also been sent to your email along with the packing details. Thank you for choosing our service.",
                    submission.getCustomerName(),
                    submission.getState(),
                    submission.getModel()
                );

                Message message = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(fromNumber),
                    messageBody
                ).create();

                log.info("SMS sent successfully. SID: {}", message.getSid());
                submission.setSmsSent(true);
            } catch (ApiException e) {
                if (e.getCode() == 21219) { // Invalid 'To' Phone Number
                    log.warn("Phone number not verified in trial account: {}. SMS will not be sent.", phoneNumber);
                    submission.setSmsSent(false);
                } else {
                    log.error("Failed to send SMS: {}", e.getMessage());
                    submission.setSmsSent(false);
                }
            }
        } catch (Exception e) {
            log.error("Error in SMS service: {}", e.getMessage());
            submission.setSmsSent(false);
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (StringUtils.isEmpty(phoneNumber)) {
            return "";
        }
        // Remove any non-digit characters
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
        
        // Add +91 prefix if not present and number is 10 digits
        if (phoneNumber.length() == 10) {
            return "+91" + phoneNumber;
        }
        // If number already has country code
        else if (phoneNumber.length() > 10) {
            return "+" + phoneNumber;
        }
        return phoneNumber;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        if (StringUtils.isEmpty(phoneNumber)) {
            return false;
        }
        // Validate international phone number format
        return phoneNumber.matches("^\\+[1-9]\\d{1,14}$");
    }
} 