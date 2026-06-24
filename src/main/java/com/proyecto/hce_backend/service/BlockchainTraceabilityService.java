package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.FabricTraceResultDTO;
import com.proyecto.hce_backend.dto.TraceabilityRegisterRequestDTO;
import com.proyecto.hce_backend.dto.TraceabilityVerifyRequestDTO;
import com.proyecto.hce_backend.dto.TraceabilityVerifyResponseDTO;
import com.proyecto.hce_backend.model.AtencionMedica;
import com.proyecto.hce_backend.model.Cita;
import com.proyecto.hce_backend.model.Paciente;
import com.proyecto.hce_backend.model.TrazabilidadBlockchain;
import com.proyecto.hce_backend.repository.AtencionMedicaRepository;
import com.proyecto.hce_backend.repository.CitaRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import com.proyecto.hce_backend.repository.TrazabilidadBlockchainRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlockchainTraceabilityService {

    private final TrazabilidadBlockchainRepository trazabilidadRepository;
    private final AtencionMedicaRepository atencionMedicaRepository;
    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;
    private final HashService hashService;
    private final FabricClientService fabricClientService;

    public BlockchainTraceabilityService(TrazabilidadBlockchainRepository trazabilidadRepository,
                                         AtencionMedicaRepository atencionMedicaRepository,
                                         PacienteRepository pacienteRepository,
                                         CitaRepository citaRepository,
                                         HashService hashService,
                                         FabricClientService fabricClientService) {
        this.trazabilidadRepository = trazabilidadRepository;
        this.atencionMedicaRepository = atencionMedicaRepository;
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
        this.hashService = hashService;
        this.fabricClientService = fabricClientService;
    }

    public List<TrazabilidadBlockchain> listar() {
        return trazabilidadRepository.findAllByOrderByFechaRegistroDesc();
    }

    public List<TrazabilidadBlockchain> consultar(String entityType, String entityId) {
        return trazabilidadRepository.findByEntityTypeAndEntityIdOrderByFechaRegistroDesc(
                hashService.normalizarTipoEntidad(entityType),
                entityId
        );
    }

    public TrazabilidadBlockchain registrar(TraceabilityRegisterRequestDTO request) {
        validarSolicitudRegistro(request);

        String tipoEntidad = hashService.normalizarTipoEntidad(request.getEntityType());
        String estado = request.getStatus() == null || request.getStatus().isBlank()
                ? "REGISTRADO"
                : request.getStatus().trim().toUpperCase();

        TrazabilidadBlockchain anterior = trazabilidadRepository
                .findTopByEntityTypeAndEntityIdOrderByFechaRegistroDesc(tipoEntidad, request.getEntityId())
                .orElse(null);

        TrazabilidadBlockchain trazabilidad = new TrazabilidadBlockchain();
        trazabilidad.setEntityType(tipoEntidad);
        trazabilidad.setEntityId(request.getEntityId());
        trazabilidad.setAction(request.getAction().trim().toUpperCase());
        trazabilidad.setUserId(request.getUserId());
        trazabilidad.setStatus(estado);
        trazabilidad.setTipoOperacion(trazabilidad.getAction());
        trazabilidad.setHashRegistro(request.getHash());
        trazabilidad.setHashAnterior(anterior != null ? anterior.getHashRegistro() : null);
        trazabilidad.setFechaRegistro(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());
        trazabilidad.setIntegridadValida(true);

        if ("ATENCION".equals(tipoEntidad) || "HISTORIA_CLINICA".equals(tipoEntidad)) {
            trazabilidad.setAtencionId(parseLong(request.getEntityId()));
        }

        trazabilidad.setUsuarioId(parseLong(request.getUserId()));

        FabricTraceResultDTO fabricResult = fabricClientService.registrarTrazabilidad(request);
        trazabilidad.setFabricTransactionId(fabricResult.getTransactionId());
        trazabilidad.setFabricChannel(fabricResult.getChannel());
        trazabilidad.setFabricChaincode(fabricResult.getChaincode());
        trazabilidad.setRegistradoEnFabric(fabricResult.getRegisteredInFabric());

        if (!Boolean.TRUE.equals(fabricResult.getSuccess())) {
            trazabilidad.setStatus("ERROR");
            trazabilidad.setIntegridadValida(false);
            trazabilidad.setErrorFabric(fabricResult.getErrorMessage());
        } else if (!Boolean.TRUE.equals(fabricResult.getRegisteredInFabric())) {
            trazabilidad.setErrorFabric(fabricResult.getErrorMessage());
        }

        return trazabilidadRepository.save(trazabilidad);
    }

    public TrazabilidadBlockchain registrarEntidad(String entityType, String action, Object entity, Long userId) {
        String tipoEntidad = hashService.normalizarTipoEntidad(entityType);

        TraceabilityRegisterRequestDTO request = new TraceabilityRegisterRequestDTO();
        request.setEntityType(tipoEntidad);
        request.setEntityId(obtenerIdEntidad(entity));
        request.setAction(action);
        request.setUserId(userId != null ? String.valueOf(userId) : null);
        request.setHash(hashService.generarHashEntidad(tipoEntidad, entity));
        request.setTimestamp(LocalDateTime.now());
        request.setStatus("REGISTRADO");

        return registrar(request);
    }

    public TraceabilityVerifyResponseDTO verificar(TraceabilityVerifyRequestDTO request) {
        if (request.getEntityType() == null || request.getEntityId() == null) {
            throw new RuntimeException("Tipo de entidad e id de registro son obligatorios");
        }

        String tipoEntidad = hashService.normalizarTipoEntidad(request.getEntityType());
        Object entidadActual = obtenerEntidadActual(tipoEntidad, request.getEntityId());
        String hashActual = hashService.generarHashEntidad(tipoEntidad, entidadActual);

        String hashRegistrado = fabricClientService
                .consultarHashRegistrado(tipoEntidad, request.getEntityId())
                .orElseGet(() -> trazabilidadRepository
                        .findTopByEntityTypeAndEntityIdOrderByFechaRegistroDesc(tipoEntidad, request.getEntityId())
                        .map(TrazabilidadBlockchain::getHashRegistro)
                        .orElse(null));

        if (hashRegistrado == null) {
            return new TraceabilityVerifyResponseDTO(
                    tipoEntidad,
                    request.getEntityId(),
                    hashActual,
                    null,
                    "SIN_REGISTRO",
                    false,
                    "No existe trazabilidad registrada para este dato clinico."
            );
        }

        boolean integro = hashActual.equals(hashRegistrado);
        String estado = integro ? "VERIFICADO" : "ALTERADO";

        trazabilidadRepository
                .findTopByEntityTypeAndEntityIdOrderByFechaRegistroDesc(tipoEntidad, request.getEntityId())
                .ifPresent(trazabilidad -> {
                    trazabilidad.setIntegridadValida(integro);
                    trazabilidad.setStatus(estado);
                    trazabilidadRepository.save(trazabilidad);
                });

        return new TraceabilityVerifyResponseDTO(
                tipoEntidad,
                request.getEntityId(),
                hashActual,
                hashRegistrado,
                estado,
                integro,
                integro
                        ? "Integro. El hash actual coincide con el hash registrado."
                        : "Integridad comprometida. El hash actual no coincide con el registrado."
        );
    }

    private Object obtenerEntidadActual(String entityType, String entityId) {
        Long id = parseLongObligatorio(entityId);

        if ("ATENCION".equals(entityType) || "HISTORIA_CLINICA".equals(entityType)) {
            return atencionMedicaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Atencion medica no encontrada"));
        }

        if ("PACIENTE".equals(entityType)) {
            return pacienteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        }

        if ("CITA".equals(entityType)) {
            return citaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        }

        throw new RuntimeException("Tipo de entidad no soportado para trazabilidad");
    }

    private void validarSolicitudRegistro(TraceabilityRegisterRequestDTO request) {
        if (request.getEntityType() == null || request.getEntityType().isBlank()) {
            throw new RuntimeException("El tipo de entidad es obligatorio");
        }

        if (request.getEntityId() == null || request.getEntityId().isBlank()) {
            throw new RuntimeException("El id del registro es obligatorio");
        }

        if (request.getAction() == null || request.getAction().isBlank()) {
            throw new RuntimeException("La accion es obligatoria");
        }

        if (request.getHash() == null || request.getHash().isBlank()) {
            throw new RuntimeException("El hash es obligatorio");
        }
    }

    private String obtenerIdEntidad(Object entity) {
        if (entity instanceof AtencionMedica atencion) {
            return String.valueOf(atencion.getId());
        }

        if (entity instanceof Paciente paciente) {
            return String.valueOf(paciente.getId());
        }

        if (entity instanceof Cita cita) {
            return String.valueOf(cita.getId());
        }

        throw new RuntimeException("Entidad no soportada para trazabilidad");
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLongObligatorio(String value) {
        Long id = parseLong(value);

        if (id == null) {
            throw new RuntimeException("El id del registro debe ser numerico");
        }

        return id;
    }
}
