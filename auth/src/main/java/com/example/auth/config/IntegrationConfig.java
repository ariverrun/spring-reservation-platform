package com.example.auth.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Transformers;
import org.springframework.messaging.MessageChannel;

@Configuration
public class IntegrationConfig {

    @Bean
    public MessageChannel userRegistrationChannel() {
        return new DirectChannel();
    }

    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange("user.events.exchange", true, false);
    }

    @Bean
    public Queue userRegistrationQueue() {
        return new Queue("user.registration.queue", true);
    }

    @Bean
    public Binding userRegistrationBinding() {
        return BindingBuilder
            .bind(userRegistrationQueue())
            .to(userEventsExchange())
            .with("user.registered");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }

    @Bean
    public IntegrationFlow userRegistrationFlow(
            MessageChannel userRegistrationChannel,
            TopicExchange userEventsExchange,
            RabbitTemplate rabbitTemplate) {
        
        return IntegrationFlow
            .from(userRegistrationChannel)
            .transform(Transformers.toJson())
            .handle(Amqp.outboundAdapter(rabbitTemplate)
                .exchangeName(userEventsExchange.getName())
                .routingKey("user.registered"))
            .get();
    }
}