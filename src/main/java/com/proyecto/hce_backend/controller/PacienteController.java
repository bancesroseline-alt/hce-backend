package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.dto.PacienteDTO;
import com.proyecto.hce_backend.model.Paciente;
import com.proyecto.hce_backend.service.PacienteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @PostMapping
    public PacienteDTO registrarPaciente(@RequestBody Paciente paciente) {
        return pacienteService.registrarPaciente(paciente);
    }

    @GetMapping
    public List<PacienteDTO> listarPacientes() {
        return pacienteService.listarPacientes();
    }

    @GetMapping("/{id}")
    public PacienteDTO obtenerPacientePorId(@PathVariable Long id) {
        return pacienteService.obtenerPacientePorId(id);
    }

    @GetMapping("/buscar")
    public List<PacienteDTO> buscarPacientes(@RequestParam String criterio) {
        return pacienteService.buscarPacientes(criterio);
    }

    @PutMapping("/{id}")
    public PacienteDTO editarPaciente(@PathVariable Long id, @RequestBody Paciente paciente) {
        return pacienteService.editarPaciente(id, paciente);
    }

    @PatchMapping("/{id}/baja")
    public PacienteDTO darDeBajaPaciente(@PathVariable Long id) {
        return pacienteService.darDeBajaPaciente(id);
    }

    @GetMapping("/total")
    public Long totalPacientes() {
        return pacienteService.totalPacientes();
    }
}