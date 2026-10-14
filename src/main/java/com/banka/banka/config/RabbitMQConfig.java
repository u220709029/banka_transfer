package com.banka.banka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "transfer.notification.queue";
    public static final String EXCHANGE_NAME = "banka.exchange";
    public static final String ROUTING_KEY = "transfer.notification.routing.key";

    @Bean
    public Queue transferNotificationQueue() {
        // durable: true -> RabbitMQ yeniden başlasa bile kuyruk silinmez
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange bankaExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding transferBinding(Queue transferNotificationQueue, TopicExchange bankaExchange) {
        return BindingBuilder
                .bind(transferNotificationQueue)
                .to(bankaExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
