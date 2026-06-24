package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.model.AtencionMedica;
import com.proyecto.hce_backend.model.TrazabilidadBlockchain;
import com.proyecto.hce_backend.repository.TrazabilidadBlockchainRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockchainService {

    private final TrazabilidadBlockchainRepository trazabilidadRepository;
    private final HashService hashService;
    private final BlockchainTraceabilityService traceabilityService;

    public BlockchainService(TrazabilidadBlockchainRepository trazabilidadRepository,
                             HashService hashService,
                             BlockchainTraceabilityService traceabilityService) {
        this.trazabilidadRepository = trazabilidadRepository;
        this.hashService = hashService;
        this.traceabilityService = traceabilityService;
    }

    public String generarHashAtencion(AtencionMedica atencion) {
        return hashService.generarHashAtencion(atencion);
    }

    public TrazabilidadBlockchain registrarTrazabilidad(AtencionMedica atencion, String tipoOperacion) {
        Long usuarioId = atencion.getUsuario() != null ? atencion.getUsuario().getId() : null;
        return traceabilityService.registrarEntidad("ATENCION", tipoOperacion, atencion, usuarioId);
    }

    public Boolean validarIntegridad(AtencionMedica atencion) {
        String hashActual = generarHashAtencion(atencion);
        return trazabilidadRepository
                .findTopByEntityTypeAndEntityIdOrderByFechaRegistroDesc("ATENCION", String.valueOf(atencion.getId()))
                .map(trazabilidad -> hashActual.equals(trazabilidad.getHashRegistro()))
                .orElseGet(() -> hashActual.equals(atencion.getHashBlockchain()));
    }

    public List<TrazabilidadBlockchain> historialPorAtencion(Long atencionId) {
        List<TrazabilidadBlockchain> historial = trazabilidadRepository
                .findByEntityTypeAndEntityIdOrderByFechaRegistroDesc("ATENCION", String.valueOf(atencionId));

        if (!historial.isEmpty()) {
            return historial;
        }

        return trazabilidadRepository.findByAtencionId(atencionId);
    }
}
