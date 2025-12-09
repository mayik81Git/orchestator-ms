package com.mayik.orchestrator_ms.saga;

import com.mayik.orchestrator_ms.repository.SagaRepository;
import com.mayik.orchestrator_ms.repository.entity.SagaLog;
import com.mayik.orchestrator_ms.saga.model.Order;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrchestratorListener {

    private final RabbitTemplate rabbitTemplate;
    private final SagaRepository sagaRepository;

    public OrchestratorListener(RabbitTemplate rabbitTemplate, SagaRepository sagaRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.sagaRepository = sagaRepository;
    }

    // Escucha el evento inicial del pedido
    @RabbitListener(queues = "orchestrator-order-created-queue")
    public void handleOrderCreated(@Payload Order order) {
        System.out.println("Orquestador: Recibido evento de Pedido Creado para el pedido " + order.getId());

        String sagaId = UUID.randomUUID().toString();
        SagaLog sagaLog = new SagaLog();
        sagaLog.setSagaId(sagaId);
        sagaLog.setOrderId(order.getId());
        sagaLog.setStatus(SagaLog.SagaStatus.IN_PROGRESS);
        sagaLog.setStep(SagaLog.SagaStep.ORDER_CREATED);
        sagaRepository.save(sagaLog);

        order.setStatus(sagaId); // Usar el ID del Saga para correlacionar los mensajes
        rabbitTemplate.convertAndSend("inventory-exchange", "inventory.reserve", order);
        System.out.println("Orquestador: Enviado comando para reservar inventario. Saga ID: " + sagaId);
    }

    // Escucha el resultado de la reserva de inventario
    @RabbitListener(queues = "orchestrator-inventory-reserved-queue")
    public void handleInventoryReserved(@Payload Order order) {
        String sagaId = order.getStatus();
        SagaLog sagaLog = sagaRepository.findById(sagaId).orElseThrow();

        System.out.println("Orquestador: Inventario reservado con éxito. Saga ID: " + sagaId);
        sagaLog.setStep(SagaLog.SagaStep.INVENTORY_RESERVED);
        // ... (aquí irían más pasos del saga, como el pago)

        sagaLog.setStep(SagaLog.SagaStep.SAGA_COMPLETED);
        sagaLog.setStatus(SagaLog.SagaStatus.COMPLETED);
        sagaRepository.save(sagaLog);

        rabbitTemplate.convertAndSend("notification-fanout-exchange", "", order);
        System.out.println("Orquestador: Saga completada, enviada notificación. Saga ID: " + sagaId);
    }

    // Escucha el fallo en la reserva de inventario
    @RabbitListener(queues = "orchestrator-inventory-failed-queue")
    public void handleInventoryFailed(@Payload Order order) {
        String sagaId = order.getStatus();
        SagaLog sagaLog = sagaRepository.findById(sagaId).orElseThrow();

        System.out.println("Orquestador: Fallo en la reserva de inventario. Saga ID: " + sagaId);
        sagaLog.setStep(SagaLog.SagaStep.INVENTORY_FAILED);
        sagaLog.setStatus(SagaLog.SagaStatus.FAILED);
        sagaRepository.save(sagaLog);

        // Iniciar transacción de compensación
        rabbitTemplate.convertAndSend("order-compensation-exchange", "order.cancel", order);
        System.out.println("Orquestador: Iniciando compensación. Saga ID: " + sagaId);
    }
}
