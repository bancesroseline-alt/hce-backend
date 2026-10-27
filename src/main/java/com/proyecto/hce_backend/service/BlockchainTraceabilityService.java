package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.FabricTraceResultDTO;
import com.proyecto.hce_backend.dto.LedgerBloqueDTO;
import com.proyecto.hce_backend.dto.LedgerEstadoDTO;
import com.proyecto.hce_backend.dto.TraceRecordDTO;
import com.proyecto.hce_backend.dto.TraceabilityRegisterRequestDTO;
import com.proyecto.hce_backend.dto.TraceabilityVerifyRequestDTO;
import com.proyecto.hce_backend.dto.TraceabilityVerifyResponseDTO;
import com.proyecto.hce_backend.model.AtencionMedica;
import com.proyecto.hce_backend.model.Cita;
import com.proyecto.hce_backend.model.LedgerBloque;
import com.proyecto.hce_backend.model.Paciente;
import com.proyecto.hce_backend.model.TrazabilidadBlockchain;
import com.proyecto.hce_backend.model.Usuario;
import com.proyecto.hce_backend.repository.AtencionMedicaRepository;
import com.proyecto.hce_backend.repository.CitaRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import com.proyecto.hce_backend.repository.TrazabilidadBlockchainRepository;
import com.proyecto.hce_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class BlockchainTraceabilityService {

    private static final int MAX_REGISTROS_SELECTOR = 100;

    private final TrazabilidadBlockchainRepository trazabilidadRepository;
    private final AtencionMedicaRepository atencionMedicaRepository;
    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HashService hashService;
    private final FabricClientService fabricClientService;
    private final LedgerService ledgerService;

    public BlockchainTraceabilityService(TrazabilidadBlockchainRepository trazabilidadRepository,
                                         AtencionMedicaRepository atencionMedicaRepository,
                                         PacienteRepository pacienteRepository,
                                         CitaRepository citaRepository,
                                         UsuarioRepository usuarioRepository,
                                         HashService hashService,
                                         FabricClientService fabricClientService,
                                         LedgerService ledgerService) {
        this.trazabilidadRepository = trazabilidadRepository;
        this.atencionMedicaRepository = atencionMedicaRepository;
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
        this.hashService = hashService;
        this.fabricClientService = fabricClientService;
        this.ledgerService = ledgerService;
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

    /** Uso interno: el registro de evidencias lo origina el backend al guardar datos clinicos. */
    public TrazabilidadBlockchain registrar(TraceabilityRegisterRequestDTO request) {
        validarSolicitudRegistro(request);

        String tipoEntidad = hashService.normalizarTipoEntidad(request.getEntityType());
        String estado = request.getStatus() == null || request.getStatus().isBlank()
                ? "REGISTRADO"
                : request.getStatus().trim().toUpperCase();

        TrazabilidadBlockchain anterior = trazabilidadRepository
                .findTopByEntityTypeAndEntityIdOrderByFechaRegistroDesc(tipoEntidad, request.getEntityId())
                .orElse(null);

        request.setEntityType(tipoEntidad);

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
        trazabilidad.setOperationId(request.getOperationId());

        if ("ATENCION".equals(tipoEntidad) || "HISTORIA_CLINICA".equals(tipoEntidad)) {
            trazabilidad.setAtencionId(parseLong(request.getEntityId()));
        }

        trazabilidad.setUsuarioId(parseLong(request.getUserId()));

        FabricTraceResultDTO fabricResult = fabricClientService.registrarTrazabilidad(request);
        trazabilidad.setFabricTransactionId(fabricResult.getTransactionId());
        trazabilidad.setFabricChannel(fabricResult.getChannel());
        trazabilidad.setFabricChaincode(fabricResult.getChaincode());
        trazabilidad.setRegistradoEnFabric(fabricResult.getRegisteredInFabric());
        trazabilidad.setVersion(fabricResult.getVersion());
        trazabilidad.setModoLedger(fabricResult.getMode());

        if (!Boolean.TRUE.equals(fabricResult.getSuccess())) {
            trazabilidad.setStatus("ERROR");
            trazabilidad.setIntegridadValida(false);
            trazabilidad.setErrorFabric(fabricResult.getErrorMessage());
        } else {
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
        request.setOperationId(UUID.randomUUID().toString());

        return registrar(request);
    }

    /**
     * verifyIntegrity(): compara el hash recalculado con la evidencia registrada en el ledger.
     * Si no hay evidencia o el ledger no es consistente, se informa esa condicion sin concluir sobre el dato.
     */
    public TraceabilityVerifyResponseDTO verificar(TraceabilityVerifyRequestDTO request) {
        if (request.getEntityType() == null || request.getEntityId() == null) {
            throw new RuntimeException("Tipo de entidad e id de registro son obligatorios");
        }

        String tipoEntidad = hashService.normalizarTipoEntidad(request.getEntityType());
        Object entidadActual = obtenerEntidadActual(tipoEntidad, request.getEntityId());
        String hashActual = hashService.generarHashEntidad(tipoEntidad, entidadActual);

        Optional<LedgerBloque> evidencia = ledgerService.getEvidence(tipoEntidad, request.getEntityId());

        TraceabilityVerifyResponseDTO respuesta = new TraceabilityVerifyResponseDTO();
        respuesta.setEntityType(tipoEntidad);
        respuesta.setEntityId(request.getEntityId());
        respuesta.setHashActual(hashActual);
        respuesta.setModoLedger(LedgerService.MODO);

        if (evidencia.isEmpty()) {
            respuesta.setEstado("SIN_REGISTRO");
            respuesta.setIntegro(false);
            respuesta.setMensaje("No existe evidencia registrada para este dato clinico. No se puede emitir una conclusion de integridad.");
            return respuesta;
        }

        LedgerBloque bloque = evidencia.get();
        respuesta.setHashRegistrado(bloque.getHash());
        respuesta.setVersion(bloque.getVersion());
        respuesta.setNumeroBloque(bloque.getNumeroBloque());
        respuesta.setTxId(bloque.getTxId());
        respuesta.setFechaEvidencia(bloque.getFecha());

        Optional<Long> bloqueInvalido = ledgerService.primerBloqueInvalido();
        if (bloqueInvalido.isPresent()) {
            respuesta.setEstado("LEDGER_ALTERADO");
            respuesta.setIntegro(false);
            respuesta.setMensaje("La cadena del ledger no es consistente (primer bloque afectado: "
                    + bloqueInvalido.get() + "). No se emite una conclusion sobre el dato clinico.");
            return respuesta;
        }

        boolean coincide = hashActual.equals(bloque.getHash());
        respuesta.setEstado(coincide ? "VERIFICADO" : "DISCREPANCIA");
        respuesta.setIntegro(coincide);
        respuesta.setMensaje(coincide
                ? "El hash recalculado coincide con la evidencia registrada en el ledger (version "
                + bloque.getVersion() + ")."
                : "El hash recalculado no coincide con la evidencia registrada. Revise el contenido del registro y su procesamiento.");

        return respuesta;
    }

    /** getHistory(): evidencias de una entidad con la validez de cada bloque del ledger. */
    public List<LedgerBloqueDTO> historialLedger(String entityType, String entityId) {
        String tipoEntidad = hashService.normalizarTipoEntidad(entityType);
        Map<Long, Boolean> validez = ledgerService.validezPorBloque();
        List<LedgerBloqueDTO> resultado = new ArrayList<>();

        for (LedgerBloque bloque : ledgerService.getHistory(tipoEntidad, entityId)) {
            LedgerBloqueDTO dto = new LedgerBloqueDTO();
            dto.setNumeroBloque(bloque.getNumeroBloque());
            dto.setTxId(bloque.getTxId());
            dto.setOperationId(bloque.getOperationId());
            dto.setCanal(bloque.getCanal());
            dto.setContrato(bloque.getContrato());
            dto.setEntityType(bloque.getEntityType());
            dto.setEntityId(bloque.getEntityId());
            dto.setVersion(bloque.getVersion());
            dto.setHash(bloque.getHash());
            dto.setAction(bloque.getAction());
            dto.setActorId(bloque.getActorId());
            dto.setActorNombre(nombreActor(bloque.getActorId()));
            dto.setEvidenciaPreviaTx(bloque.getEvidenciaPreviaTx());
            dto.setFecha(bloque.getFecha());
            dto.setEndosos(bloque.getEndosos());
            dto.setHashBloqueAnterior(bloque.getHashBloqueAnterior());
            dto.setHashBloque(bloque.getHashBloque());
            dto.setBloqueValido(validez.getOrDefault(bloque.getNumeroBloque(), false));
            resultado.add(dto);
        }

        return resultado;
    }

    public LedgerEstadoDTO estadoLedger() {
        return ledgerService.estado();
    }

    /**
     * Registros que se pueden elegir para consultar su trazabilidad, para que el usuario no tenga que conocer ids.
     * Para el administrador se ocultan los datos de las personas (solo identificadores y fechas).
     */
    public List<TraceRecordDTO> listarRegistros(String entityType, String filtro, boolean ocultarDatosPersonales) {
        String tipoEntidad = hashService.normalizarTipoEntidad(entityType);
        String criterio = filtro == null ? "" : filtro.trim().toLowerCase(Locale.ROOT);
        List<TraceRecordDTO> registros = new ArrayList<>();

        if ("ATENCION".equals(tipoEntidad) || "HISTORIA_CLINICA".equals(tipoEntidad)) {
            for (AtencionMedica a : atencionMedicaRepository.findAll()) {
                registros.add(registro(
                        "ATENCION",
                        a.getId(),
                        "Atencion #" + a.getId(),
                        descripcionPaciente(a.getPaciente(), ocultarDatosPersonales)
                                + (a.getTipoAtencion() != null ? " · " + a.getTipoAtencion() : ""),
                        a.getFechaHora()
                ));
            }
        } else if ("PACIENTE".equals(tipoEntidad)) {
            for (Paciente p : pacienteRepository.findAll()) {
                registros.add(registro(
                        "PACIENTE",
                        p.getId(),
                        "Paciente #" + p.getId(),
                        descripcionPaciente(p, ocultarDatosPersonales),
                        p.getFechaSincronizacion()
                ));
            }
        } else if ("CITA".equals(tipoEntidad)) {
            for (Cita c : citaRepository.findAll()) {
                LocalDateTime fecha = c.getFecha() != null
                        ? (c.getHora() != null ? c.getFecha().atTime(c.getHora()) : c.getFecha().atStartOfDay())
                        : null;

                registros.add(registro(
                        "CITA",
                        c.getId(),
                        "Cita #" + c.getId(),
                        descripcionPaciente(c.getPaciente(), ocultarDatosPersonales)
                                + (c.getEspecialidad() != null ? " · " + c.getEspecialidad() : ""),
                        fecha
                ));
            }
        } else {
            throw new RuntimeException("Tipo de entidad no soportado para trazabilidad");
        }

        return registros.stream()
                .filter(r -> criterio.isEmpty()
                        || (r.getTitulo() + " " + r.getDetalle()).toLowerCase(Locale.ROOT).contains(criterio))
                .sorted(Comparator.comparing(TraceRecordDTO::getFecha,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(MAX_REGISTROS_SELECTOR)
                .toList();
    }

    private TraceRecordDTO registro(String tipo, Long id, String titulo, String detalle, LocalDateTime fecha) {
        String idTexto = String.valueOf(id);
        return new TraceRecordDTO(tipo, idTexto, titulo, detalle, fecha, ledgerService.contarEvidencias(tipo, idTexto));
    }

    private String descripcionPaciente(Paciente paciente, boolean ocultar) {
        if (paciente == null) {
            return "Sin paciente";
        }

        String documento = paciente.getNumeroDocumento() == null ? "" : paciente.getNumeroDocumento();

        if (ocultar) {
            String final3 = documento.length() > 3 ? documento.substring(documento.length() - 3) : documento;
            return "Paciente DNI ****" + final3;
        }

        return ((paciente.getNombres() == null ? "" : paciente.getNombres()) + " "
                + (paciente.getApellidos() == null ? "" : paciente.getApellidos())).trim()
                + (documento.isEmpty() ? "" : " · DNI " + documento);
    }

    private String nombreActor(String actorId) {
        Long id = parseLong(actorId);

        if (id == null) {
            return "Sistema";
        }

        return usuarioRepository.findById(id)
                .map((Usuario u) -> ((u.getNombres() == null ? "" : u.getNombres()) + " "
                        + (u.getApellidos() == null ? "" : u.getApellidos())).trim())
                .filter(nombre -> !nombre.isEmpty())
                .orElse("Usuario " + id);
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
