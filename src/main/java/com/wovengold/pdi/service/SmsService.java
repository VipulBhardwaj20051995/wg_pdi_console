package com.wovengold.pdi.service;

import com.wovengold.pdi.model.PDISubmission;

public interface SmsService {
    void sendSms(PDISubmission submission);
} 