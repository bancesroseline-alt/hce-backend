package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.dto.SincronizacionCitaRequestDTO;
import com.proyecto.hce_backend.dto.SincronizacionRequestDTO;
import com.proyecto.hce_backend.model.AtencionMedica;
import com.proyecto.hce_backend.model.Cita;
import com.proyecto.hce_backend.model.Paciente;
import com.proyecto.hce_backend.service.SincronizacionCitaService;
import com.proyecto.hce_backend.service.SincronizacionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sincronizacion")
@SecurityRequirement(name = "bearerAuth")
public class SincronizacionController {

    final SincronizacionService sincronizacionService;

    final SincronizacionCitaService sincronizacionCitaService;

    public SincronizacionController(SincronizacionService sincronizacionService,
                                    SincronizacionCitaService sincronizacionCitaService) {
        this.sincronizacionService = sincronizacionService;
        this.sincronizacionCitaService = sincronizacionCitaService;
    }

    @PostMapping
    public AtencionMedica sincronizarAtencion(@RequestBody SincronizacionRequestDTO dto) {
        return sincronizacionService.sincronizarAtencion(dto);
    }

    @PostMapping("/citas")
    public Cita sincronizarCita(@RequestBody SincronizacionCitaRequestDTO dto) {
        return sincronizacionCitaService.sincronizarCita(dto);
    }

    @PostMapping("/pacientes")
    public Paciente sincronizarPaciente(@RequestBody Paciente paciente) {
        return sincronizacionService.sincronizarPaciente(paciente);
    }
}
