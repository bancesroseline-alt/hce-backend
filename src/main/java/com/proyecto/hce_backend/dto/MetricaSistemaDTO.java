package com.proyecto.hce_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MetricaSistemaDTO {

    private Long totalUsuarios;
    private Long totalLogs;
    private Long totalErrores;
    private Long totalFeedback;
    private Long memoriaUsadaMb;
    private Long memoriaDisponibleMb;
    private Long uptimeSegundos;
    private Double tasaErrores;

    public MetricaSistemaDTO(Long totalUsuarios, Long totalLogs, Long totalErrores, Long totalFeedback,
                             Long memoriaUsadaMb, Long memoriaDisponibleMb, Long uptimeSegundos,
                             Double tasaErrores) {
        this.totalUsuarios = totalUsuarios;
        this.totalLogs = totalLogs;
        this.totalErrores = totalErrores;
        this.totalFeedback = totalFeedback;
        this.memoriaUsadaMb = memoriaUsadaMb;
        this.memoriaDisponibleMb = memoriaDisponibleMb;
        this.uptimeSegundos = uptimeSegundos;
        this.tasaErrores = tasaErrores;
    }
}
