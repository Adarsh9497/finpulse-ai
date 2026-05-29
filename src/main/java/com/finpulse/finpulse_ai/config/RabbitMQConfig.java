package com.finpulse.finpulse_ai.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String INGESTION_QUEUE = "ingestion.queue";
    public static final String INGESTION_EXCHANGE = "ingestion.exchange";
    public static final String INGESTION_ROUTING_KEY = "ingestion.routing.key";

    @Bean
    public Queue queue() {
        return new Queue(INGESTION_QUEUE, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(INGESTION_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(INGESTION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
