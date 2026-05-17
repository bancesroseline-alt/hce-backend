package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.dto.ReporteGeneralDTO;
import com.proyecto.hce_backend.model.Cita;
import com.proyecto.hce_backend.service.ReporteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/general")
    public ReporteGeneralDTO reporteGeneral() {
        return reporteService.reporteGeneral();
    }

    @GetMapping("/citas")
    public List<Cita> reporteCitas() {
        return reporteService.reporteCitas();
    }

    @GetMapping("/inasistencias")
    public List<Cita> reporteInasistencias() {
        return reporteService.reporteInasistencias();
    }
}