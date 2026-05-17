package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricaSistemaDTO {

    private Long totalUsuarios;
    private Long totalLogs;
    private Long totalErrores;
    private Long totalFeedback;
}