package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerBloqueDTO {

    private Long numeroBloque;
    private String txId;
    private String operationId;
    private String canal;
    private String contrato;
    private String entityType;
    private String entityId;
    private Integer version;
    private String hash;
    private String action;
    private String actorId;
    private String actorNombre;
    private String evidenciaPreviaTx;
    private LocalDateTime fecha;
    private String endosos;
    private String hashBloqueAnterior;
    private String hashBloque;
    private Boolean bloqueValido;
}
