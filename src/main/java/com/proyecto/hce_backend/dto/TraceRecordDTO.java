package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Registro clinico que se puede elegir en la pantalla de trazabilidad (sin pedir ids al usuario). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceRecordDTO {

    private String entityType;
    private String entityId;
    private String titulo;
    private String detalle;
    private LocalDateTime fecha;
    private long evidencias;
}
