package com.example.notification.service;

import com.example.notification.event.UserRegisteredEvent;

public interface EmailService {
    void sendWelcomeEmail(UserRegisteredEvent event);
}