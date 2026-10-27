package com.proyecto.hce_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Bloque del ledger de evidencias (modo simulado de Hyperledger Fabric).
 * Es de solo anexado: no existen operaciones de actualizacion ni borrado.
 */
@Entity
@Table(name = "ledger_bloque")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerBloque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long numeroBloque;

    @Column(nullable = false, unique = true, length = 64)
    private String txId;

    @Column(nullable = false, unique = true)
    private String operationId;

    private String canal;

    private String contrato;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private String entityId;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, length = 64)
    private String hash;

    @Column(nullable = false)
    private String action;

    private String actorId;

    private String evidenciaPreviaTx;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private String endosos;

    @Column(nullable = false, length = 64)
    private String hashBloqueAnterior;

    @Column(nullable = false, length = 64)
    private String hashBloque;
}
