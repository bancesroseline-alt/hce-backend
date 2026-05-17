package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.dto.HistoriaClinicaDTO;
import com.proyecto.hce_backend.service.HistoriaClinicaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/historias-clinicas")
public class HistoriaClinicaController {

    @Autowired
    private HistoriaClinicaService historiaClinicaService;

    @GetMapping("/paciente/{pacienteId}")
    public HistoriaClinicaDTO obtenerHistoriaClinica(@PathVariable Long pacienteId) {
        return historiaClinicaService.obtenerHistoriaClinicaPorPaciente(pacienteId);
    }
}