package com.wovengold.pdi.service.impl;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.wovengold.pdi.model.PDISubmission;
import com.wovengold.pdi.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

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
            Twilio.init(accountSid, authToken);
            
            String messageBody = String.format(
                "Hi %s from %s! Welcome to Wovengold! Your model %s will be packed today. The packing Video has also been sent to you email along with the packing details. Thank you for choosing our service.",
                submission.getCustomerName(),
                submission.getState(),
                submission.getModel()
            );

            Message.creator(
                new PhoneNumber("+91" + submission.getCustomerPhone()),
                new PhoneNumber(fromNumber),
                messageBody
            ).create();

            log.info("SMS sent successfully to customer: {}", submission.getCustomerPhone());
            submission.setSmsSent(true);
        } catch (Exception e) {
            log.error("Failed to send SMS to customer {}: {}", submission.getCustomerPhone(), e.getMessage());
            submission.setSmsSent(false);
        }
    }
} 