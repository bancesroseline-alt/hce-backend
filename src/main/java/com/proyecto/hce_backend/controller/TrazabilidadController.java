package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.model.AtencionMedica;
import com.proyecto.hce_backend.model.TrazabilidadBlockchain;
import com.proyecto.hce_backend.repository.AtencionMedicaRepository;
import com.proyecto.hce_backend.service.BlockchainService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trazabilidad")
@SecurityRequirement(name = "bearerAuth")
public class TrazabilidadController {

    final BlockchainService blockchainService;
    final AtencionMedicaRepository atencionMedicaRepository;

    public TrazabilidadController(BlockchainService blockchainService,
                                  AtencionMedicaRepository atencionMedicaRepository) {
        this.blockchainService = blockchainService;
        this.atencionMedicaRepository = atencionMedicaRepository;
    }

    @GetMapping("/atencion/{id}")
    public List<TrazabilidadBlockchain> historialPorAtencion(@PathVariable Long id) {
        return blockchainService.historialPorAtencion(id);
    }

    @GetMapping("/validar/{id}")
    public String validarIntegridad(@PathVariable Long id) {
        AtencionMedica atencion = atencionMedicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atención médica no encontrada"));

        Boolean valida = blockchainService.validarIntegridad(atencion);

        if (valida) {
            return "Integridad válida. El registro no ha sido alterado.";
        } else {
            return "Integridad inválida. El registro pudo haber sido alterado.";
        }
    }
}