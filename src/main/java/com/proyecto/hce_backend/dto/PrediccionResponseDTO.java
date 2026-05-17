package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrediccionResponseDTO {

    private Long pacienteId;
    private Double probabilidadInasistencia;
    private String nivelRiesgo;
    private String recomendacion;
}