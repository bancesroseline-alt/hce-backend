package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TraceabilityVerifyResponseDTO {

    private String entityType;
    private String entityId;
    private String hashActual;
    private String hashRegistrado;
    private String estado;
    private Boolean integro;
    private String mensaje;
}
