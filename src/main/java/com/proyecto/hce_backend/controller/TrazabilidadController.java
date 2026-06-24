package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.dto.TraceabilityRegisterRequestDTO;
import com.proyecto.hce_backend.dto.TraceabilityVerifyRequestDTO;
import com.proyecto.hce_backend.dto.TraceabilityVerifyResponseDTO;
import com.proyecto.hce_backend.model.AtencionMedica;
import com.proyecto.hce_backend.model.TrazabilidadBlockchain;
import com.proyecto.hce_backend.repository.AtencionMedicaRepository;
import com.proyecto.hce_backend.service.BlockchainService;
import com.proyecto.hce_backend.service.BlockchainTraceabilityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trazabilidad")
@SecurityRequirement(name = "bearerAuth")
public class TrazabilidadController {

    private final BlockchainService blockchainService;
    private final BlockchainTraceabilityService traceabilityService;
    private final AtencionMedicaRepository atencionMedicaRepository;

    public TrazabilidadController(BlockchainService blockchainService,
                                  BlockchainTraceabilityService traceabilityService,
                                  AtencionMedicaRepository atencionMedicaRepository) {
        this.blockchainService = blockchainService;
        this.traceabilityService = traceabilityService;
        this.atencionMedicaRepository = atencionMedicaRepository;
    }

    @GetMapping
    public List<TrazabilidadBlockchain> listar() {
        return traceabilityService.listar();
    }

    @PostMapping("/registrar")
    public TrazabilidadBlockchain registrar(@RequestBody TraceabilityRegisterRequestDTO request) {
        return traceabilityService.registrar(request);
    }

    @PostMapping("/verificar")
    public TraceabilityVerifyResponseDTO verificar(@RequestBody TraceabilityVerifyRequestDTO request) {
        return traceabilityService.verificar(request);
    }

    @GetMapping("/atencion/{id}")
    public List<TrazabilidadBlockchain> historialPorAtencion(@PathVariable Long id) {
        return blockchainService.historialPorAtencion(id);
    }

    @GetMapping("/validar/{id}")
    public String validarIntegridad(@PathVariable Long id) {
        AtencionMedica atencion = atencionMedicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atencion medica no encontrada"));

        Boolean valida = blockchainService.validarIntegridad(atencion);

        if (valida) {
            return "Integridad valida. El registro no ha sido alterado.";
        }

        return "Integridad invalida. El registro pudo haber sido alterado.";
    }

    @GetMapping("/{tipoEntidad}/{idRegistro}")
    public List<TrazabilidadBlockchain> historialPorEntidad(@PathVariable String tipoEntidad,
                                                            @PathVariable String idRegistro) {
        return traceabilityService.consultar(tipoEntidad, idRegistro);
    }
}
