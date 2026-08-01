package com.example.auth.eventlistener;

import com.example.auth.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final MessageChannel userRegistrationChannel;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        userRegistrationChannel.send(
            MessageBuilder.withPayload(event).build()
        );
        log.info("UserRegisteredEvent sent to RabbitMQ for user: {}", event.userId());
    }
}