package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.dto.CitaDTO;
import com.proyecto.hce_backend.dto.CitaRequestDTO;
import com.proyecto.hce_backend.model.EstadoCita;
import com.proyecto.hce_backend.service.CitaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @PostMapping
    public CitaDTO registrarCita(@RequestBody CitaRequestDTO dto) {
        return citaService.registrarCita(dto);
    }

    @GetMapping
    public List<CitaDTO> listarCitas() {
        return citaService.listarCitas();
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<CitaDTO> listarPorPaciente(@PathVariable Long pacienteId) {
        return citaService.listarPorPaciente(pacienteId);
    }

    @GetMapping("/medico/{medicoId}")
    public List<CitaDTO> listarPorMedico(@PathVariable Long medicoId) {
        return citaService.listarPorMedico(medicoId);
    }

    @PatchMapping("/{citaId}/estado")
    public CitaDTO actualizarEstado(@PathVariable Long citaId, @RequestParam EstadoCita estado) {
        return citaService.actualizarEstado(citaId, estado);
    }

    @GetMapping("/programadas")
    public List<CitaDTO> listarCitasProgramadas() {
        return citaService.listarCitasProgramadas();
    }

    @PutMapping("/{id}")
    public CitaDTO editarCita(@PathVariable Long id, @RequestBody CitaRequestDTO dto) {
        return citaService.editarCita(id, dto);
    }

    @GetMapping("/total")
    public Long totalCitas() {
        return citaService.totalCitas();
    }

    @GetMapping("/hoy")
    public Long citasHoy() {
        return citaService.citasHoy();
    }
}