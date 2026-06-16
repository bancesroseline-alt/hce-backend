package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SincronizacionRequestDTO {

    private String uuidLocal;

    private Long pacienteId;
    private Long usuarioId;
    private Long citaId;

    private LocalDateTime fechaHora;

    private String tipoAtencion;
    private String motivoConsulta;
    private String presionArterial;
    private Double temperatura;
    private Integer saturacion;
    private Double talla;
    private Double peso;
    private String diagnostico;
    private String tratamientoIndicado;
    private String observaciones;
    private String medicamentos;

    private String estado;
    private String origenRegistro;
}
