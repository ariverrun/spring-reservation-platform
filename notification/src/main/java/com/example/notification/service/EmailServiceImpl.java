package com.example.notification.service;

import com.example.notification.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public void sendWelcomeEmail(UserRegisteredEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.email());
        message.setSubject("Welcome to our service!");
        message.setText(String.format(
            "Hello %s %s,\n\nYour account has been successfully created.\n\nBest regards,\nTeam",
            event.firstName(),
            event.lastName()
        ));

        mailSender.send(message);
        log.info("Welcome email sent to: {}", event.email());
    }
}