package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrediccionPacienteDTO {

    private Long pacienteId;
    private String nombres;
    private String apellidos;
    private Double probabilidadInasistencia;
    private String nivelRiesgo;
    private String recomendacion;
    private Integer cantidadCitasPrevias;
    private Integer cantidadInasistenciasPrevias;
    private Integer cantidadReprogramaciones;
}
