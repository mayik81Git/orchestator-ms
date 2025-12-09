package com.mayik.orchestrator_ms.saga.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Order implements Serializable {
    private Long id;
    private String userId;
    private List<String> items;
    private String status;
}
