package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.dto.MetricaSistemaDTO;
import com.proyecto.hce_backend.service.MetricaSistemaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metricas")
@SecurityRequirement(name = "bearerAuth")
public class MetricaSistemaController {

    final MetricaSistemaService metricaSistemaService;

    public MetricaSistemaController(MetricaSistemaService metricaSistemaService) {
        this.metricaSistemaService = metricaSistemaService;
    }

    @GetMapping
    public MetricaSistemaDTO obtenerMetricas() {
        return metricaSistemaService.obtenerMetricas();
    }
}