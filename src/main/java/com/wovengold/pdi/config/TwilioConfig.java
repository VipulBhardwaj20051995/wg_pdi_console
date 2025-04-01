package com.wovengold.pdi.config;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class TwilioConfig {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    public String sendSms(String to, String messageBody) {
        Message message = Message.creator(
                new PhoneNumber(to),   // Receiver's phone number
                new PhoneNumber(twilioPhoneNumber), // Twilio's phone number
                messageBody // Message text
        ).create();

        return "Message sent successfully! SID: " + message.getSid();
    }

}
