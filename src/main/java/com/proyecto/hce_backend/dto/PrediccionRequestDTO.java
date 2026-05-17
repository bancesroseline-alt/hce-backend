package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrediccionRequestDTO {

    private Long pacienteId;
    private Integer edad;
    private Integer cantidadCitasPrevias;
    private Integer cantidadInasistenciasPrevias;
    private String tipoCita;
    private String especialidad;
    private Long citaId;
}