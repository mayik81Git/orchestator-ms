package com.mayik.orchestrator_ms.repository;

import com.mayik.orchestrator_ms.repository.entity.SagaLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaRepository extends JpaRepository<SagaLog, String> {
}
