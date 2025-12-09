package com.mayik.orchestrator_ms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // Exchanges
    @Bean
    public TopicExchange orderSagaExchange() { return new TopicExchange("order-saga-exchange"); }
    @Bean
    public TopicExchange inventoryExchange() { return new TopicExchange("inventory-exchange"); }
    @Bean
    public TopicExchange orderCompensationExchange() { return new TopicExchange("order-compensation-exchange"); }
    @Bean
    public FanoutExchange notificationExchange() { return new FanoutExchange("notification-fanout-exchange"); }

    // Colas del Orquestador
    @Bean
    public Queue orderCreatedQueue() { return new Queue("orchestrator-order-created-queue"); }
    @Bean
    public Queue inventoryReservedQueue() { return new Queue("orchestrator-inventory-reserved-queue"); }
    @Bean
    public Queue inventoryFailedQueue() { return new Queue("orchestrator-inventory-failed-queue"); }

    // Bindings del Orquestador
    @Bean
    public Binding bindingOrderCreated(Queue orderCreatedQueue, TopicExchange orderSagaExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(orderSagaExchange).with("order.created");
    }
    @Bean
    public Binding bindingInventoryReserved(Queue inventoryReservedQueue, TopicExchange inventoryExchange) {
        return BindingBuilder.bind(inventoryReservedQueue).to(inventoryExchange).with("inventory.reserved");
    }
    @Bean
    public Binding bindingInventoryFailed(Queue inventoryFailedQueue, TopicExchange inventoryExchange) {
        return BindingBuilder.bind(inventoryFailedQueue).to(inventoryExchange).with("inventory.failed");
    }
}
