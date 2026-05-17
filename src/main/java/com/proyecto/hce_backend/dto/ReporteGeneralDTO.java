package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteGeneralDTO {

    private Long totalPacientes;
    private Long totalCitas;
    private Long totalAtenciones;
    private Long totalInasistencias;
}