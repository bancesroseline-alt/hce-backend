package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.dto.PrediccionRequestDTO;
import com.proyecto.hce_backend.dto.PrediccionResponseDTO;
import com.proyecto.hce_backend.model.PrediccionInasistencia;
import com.proyecto.hce_backend.service.PrediccionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predicciones")
@SecurityRequirement(name = "bearerAuth")
public class PrediccionController {

    final PrediccionService prediccionService;

    public PrediccionController(PrediccionService prediccionService) {
        this.prediccionService = prediccionService;
    }

    @PostMapping("/inasistencia")
    public PrediccionResponseDTO predecirInasistencia(@RequestBody PrediccionRequestDTO dto) {
        return prediccionService.predecirInasistencia(dto);
    }

    @GetMapping("/paciente/{id}")
    public List<PrediccionInasistencia> listarPorPaciente(@PathVariable Long id) {
        return prediccionService.listarPorPaciente(id);
    }

    @GetMapping("/alertas")
    public List<PrediccionInasistencia> listarAlertas() {
        return prediccionService.listarAlertas();
    }
}