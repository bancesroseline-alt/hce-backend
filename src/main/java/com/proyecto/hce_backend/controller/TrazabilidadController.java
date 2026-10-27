package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.dto.LedgerBloqueDTO;
import com.proyecto.hce_backend.dto.LedgerEstadoDTO;
import com.proyecto.hce_backend.dto.TraceRecordDTO;
import com.proyecto.hce_backend.dto.TraceabilityVerifyRequestDTO;
import com.proyecto.hce_backend.dto.TraceabilityVerifyResponseDTO;
import com.proyecto.hce_backend.model.TrazabilidadBlockchain;
import com.proyecto.hce_backend.service.BlockchainService;
import com.proyecto.hce_backend.service.BlockchainTraceabilityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Consulta y verificacion de trazabilidad. El registro de evidencias NO se expone por HTTP:
 * solo lo origina el backend cuando se guardan datos clinicos.
 */
@RestController
@RequestMapping("/api/trazabilidad")
@SecurityRequirement(name = "bearerAuth")
public class TrazabilidadController {

    private final BlockchainService blockchainService;
    private final BlockchainTraceabilityService traceabilityService;

    public TrazabilidadController(BlockchainService blockchainService,
                                  BlockchainTraceabilityService traceabilityService) {
        this.blockchainService = blockchainService;
        this.traceabilityService = traceabilityService;
    }

    @GetMapping
    public List<TrazabilidadBlockchain> listar() {
        return traceabilityService.listar();
    }

    @PostMapping("/verificar")
    public TraceabilityVerifyResponseDTO verificar(@RequestBody TraceabilityVerifyRequestDTO request) {
        return traceabilityService.verificar(request);
    }

    /** Registros elegibles para consultar (sin pedir ids). El administrador no ve datos personales. */
    @GetMapping("/registros")
    public List<TraceRecordDTO> registros(@RequestParam(defaultValue = "ATENCION") String tipo,
                                          @RequestParam(required = false) String q,
                                          Authentication authentication) {
        boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(autoridad -> "ROLE_ADMIN".equals(autoridad.getAuthority()));

        return traceabilityService.listarRegistros(tipo, q, esAdmin);
    }

    @GetMapping("/ledger/estado")
    public LedgerEstadoDTO estadoLedger() {
        return traceabilityService.estadoLedger();
    }

    @GetMapping("/ledger/{tipoEntidad}/{idRegistro}")
    public List<LedgerBloqueDTO> historialLedger(@PathVariable String tipoEntidad,
                                                 @PathVariable String idRegistro) {
        return traceabilityService.historialLedger(tipoEntidad, idRegistro);
    }

    @GetMapping("/atencion/{id}")
    public List<TrazabilidadBlockchain> historialPorAtencion(@PathVariable Long id) {
        return blockchainService.historialPorAtencion(id);
    }

    @GetMapping("/validar/{id}")
    public String validarIntegridad(@PathVariable Long id) {
        TraceabilityVerifyRequestDTO solicitud = new TraceabilityVerifyRequestDTO();
        solicitud.setEntityType("ATENCION");
        solicitud.setEntityId(String.valueOf(id));

        return traceabilityService.verificar(solicitud).getMensaje();
    }

    @GetMapping("/{tipoEntidad}/{idRegistro}")
    public List<TrazabilidadBlockchain> historialPorEntidad(@PathVariable String tipoEntidad,
                                                            @PathVariable String idRegistro) {
        return traceabilityService.consultar(tipoEntidad, idRegistro);
    }
}
