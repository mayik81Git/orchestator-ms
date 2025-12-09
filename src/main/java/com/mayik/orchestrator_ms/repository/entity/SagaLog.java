package com.mayik.orchestrator_ms.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;

import java.io.Serializable;

@Entity
@Data
public class SagaLog implements Serializable {
    @Id
    private String sagaId;
    private Long orderId;
    @Enumerated(EnumType.STRING)
    private SagaStatus status;
    @Enumerated(EnumType.STRING)
    private SagaStep step;

    public enum SagaStatus {
        IN_PROGRESS, COMPLETED, FAILED
    }

    public enum SagaStep {
        ORDER_CREATED, INVENTORY_RESERVED, INVENTORY_FAILED, SAGA_COMPLETED, SAGA_FAILED
    }
}
