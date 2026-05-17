package com.proyecto.hce_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trazabilidad_blockchain")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrazabilidadBlockchain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long atencionId;

    private Long usuarioId;

    @Column(nullable = false)
    private String tipoOperacion;

    @Column(nullable = false, length = 1000)
    private String hashRegistro;

    private String hashAnterior;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private Boolean integridadValida;
}