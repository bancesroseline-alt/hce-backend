package com.proyecto.hce_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TraceabilityRegisterRequestDTO {

    private String entityType;
    private String entityId;
    private String action;
    private String userId;
    private String hash;
    private LocalDateTime timestamp;
    private String status;
}
