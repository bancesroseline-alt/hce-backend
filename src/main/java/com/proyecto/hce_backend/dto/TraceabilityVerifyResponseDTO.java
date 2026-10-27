package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceabilityVerifyResponseDTO {

    private String entityType;
    private String entityId;
    private String hashActual;
    private String hashRegistrado;
    private String estado;
    private Boolean integro;
    private String mensaje;
    private Integer version;
    private Long numeroBloque;
    private String txId;
    private LocalDateTime fechaEvidencia;
    private String modoLedger;
}
