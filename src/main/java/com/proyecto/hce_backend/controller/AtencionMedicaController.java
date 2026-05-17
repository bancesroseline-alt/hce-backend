package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.dto.AtencionMedicaDTO;
import com.proyecto.hce_backend.dto.AtencionMedicaRequestDTO;
import com.proyecto.hce_backend.service.AtencionMedicaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/atenciones")
public class AtencionMedicaController {

    @Autowired
    private AtencionMedicaService atencionMedicaService;

    @PostMapping
    public AtencionMedicaDTO registrarAtencion(@RequestBody AtencionMedicaRequestDTO dto) {
        return atencionMedicaService.registrarAtencion(dto);
    }

    @GetMapping
    public List<AtencionMedicaDTO> listarAtenciones() {
        return atencionMedicaService.listarAtenciones();
    }

    @GetMapping("/historia-clinica/{pacienteId}")
    public List<AtencionMedicaDTO> listarPorPaciente(@PathVariable Long pacienteId) {
        return atencionMedicaService.listarPorPaciente(pacienteId);
    }

    @PutMapping("/{id}")
    public AtencionMedicaDTO editarAtencion(@PathVariable Long id, @RequestBody AtencionMedicaRequestDTO dto) {
        return atencionMedicaService.editarAtencion(id, dto);
    }
}