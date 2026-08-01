package com.example.notification.listener;

import com.example.notification.event.UserRegisteredEvent;
import com.example.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegistrationListener {

    private final EmailService emailService;

    @RabbitListener(queues = "user.registration.queue")
    public void handleUserRegistered(@Payload UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for user: {}", event.userId());
        emailService.sendWelcomeEmail(event);
    }
}