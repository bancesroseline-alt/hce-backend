package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.AtencionMedicaDTO;
import com.proyecto.hce_backend.dto.AtencionMedicaRequestDTO;
import com.proyecto.hce_backend.model.*;
import com.proyecto.hce_backend.repository.AtencionMedicaRepository;
import com.proyecto.hce_backend.repository.CitaRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import com.proyecto.hce_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtencionMedicaService {

    @Autowired
    private AtencionMedicaRepository atencionMedicaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private BlockchainTraceabilityService traceabilityService;

    @Autowired
    private HashService hashService;

    public AtencionMedicaDTO registrarAtencion(AtencionMedicaRequestDTO dto) {

        validarCamposBase(dto);

        if (dto.getEstado() == EstadoAtencion.COMPLETADA) {
            validarCamposCompletados(dto);
        }

        validarCitaSegunTipoAtencion(dto);

        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        if (!paciente.getEstado()) {
            throw new RuntimeException("No se puede registrar atención para un paciente dado de baja");
        }

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Cita cita = null;

        if (dto.getCitaId() != null) {
            cita = citaRepository.findById(dto.getCitaId())
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        }

        AtencionMedica atencion = new AtencionMedica();
        atencion.setPaciente(paciente);
        atencion.setUsuario(usuario);
        atencion.setCita(cita);
        atencion.setFechaHora(dto.getFechaHora());
        atencion.setTipoAtencion(dto.getTipoAtencion());
        atencion.setMotivoConsulta(dto.getMotivoConsulta());
        atencion.setPresionArterial(dto.getPresionArterial());
        atencion.setTemperatura(dto.getTemperatura());
        atencion.setSaturacion(dto.getSaturacion());
        atencion.setTalla(dto.getTalla());
        atencion.setPeso(dto.getPeso());
        atencion.setDiagnostico(dto.getDiagnostico());
        atencion.setObservaciones(dto.getObservaciones());
        atencion.setTratamientoIndicado(dto.getTratamientoIndicado());
        atencion.setMedicamentos(dto.getMedicamentos());
        atencion.setEstado(dto.getEstado());

        AtencionMedica guardada = atencionMedicaRepository.save(atencion);
        guardada.setHashBlockchain(hashService.generarHashAtencion(guardada));
        guardada = atencionMedicaRepository.save(guardada);
        traceabilityService.registrarEntidad("ATENCION", "CREAR", guardada, usuario.getId());

        if (cita != null) {
            cita.setEstado(EstadoCita.ATENDIDA);
            citaRepository.save(cita);
            traceabilityService.registrarEntidad("CITA", "ACTUALIZAR", cita, usuario.getId());
        }

        return convertirADTO(guardada);
    }

    public List<AtencionMedicaDTO> listarAtenciones() {
        return atencionMedicaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<AtencionMedicaDTO> listarPorPaciente(Long pacienteId) {
        return atencionMedicaRepository.findByPacienteId(pacienteId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    private void validarCamposBase(AtencionMedicaRequestDTO dto) {
        if (dto.getPacienteId() == null) throw new RuntimeException("El paciente es obligatorio");
        if (dto.getUsuarioId() == null) throw new RuntimeException("El profesional de salud es obligatorio");
        if (dto.getFechaHora() == null) throw new RuntimeException("La fecha y hora es obligatoria");
        if (dto.getTipoAtencion() == null) throw new RuntimeException("El tipo de atención es obligatorio");
        if (dto.getMotivoConsulta() == null || dto.getMotivoConsulta().isBlank()) {
            throw new RuntimeException("El motivo de consulta es obligatorio");
        }
        if (dto.getEstado() == null) throw new RuntimeException("El estado de atención es obligatorio");
    }

    private void validarCamposCompletados(AtencionMedicaRequestDTO dto) {
        if (dto.getDiagnostico() == null || dto.getDiagnostico().isBlank()) {
            throw new RuntimeException("El diagnóstico es obligatorio para completar la atención");
        }
        if (dto.getTratamientoIndicado() == null || dto.getTratamientoIndicado().isBlank()) {
            throw new RuntimeException("El tratamiento indicado es obligatorio para completar la atención");
        }
    }

    public AtencionMedicaDTO convertirADTO(AtencionMedica atencion) {
        return new AtencionMedicaDTO(
                atencion.getId(),
                atencion.getPaciente().getId(),
                atencion.getPaciente().getNombres() + " " + atencion.getPaciente().getApellidos(),
                atencion.getUsuario().getId(),
                atencion.getUsuario().getNombres() + " " + atencion.getUsuario().getApellidos(),
                atencion.getFechaHora(),
                atencion.getTipoAtencion(),
                atencion.getMotivoConsulta(),
                atencion.getPresionArterial(),
                atencion.getTemperatura(),
                atencion.getSaturacion(),
                atencion.getTalla(),
                atencion.getPeso(),
                atencion.getDiagnostico(),
                atencion.getObservaciones(),
                atencion.getTratamientoIndicado(),
                atencion.getMedicamentos(),
                atencion.getEstado(),
                atencion.getCita() != null ? atencion.getCita().getId() : null
        );
    }

    public AtencionMedicaDTO editarAtencion(Long id, AtencionMedicaRequestDTO dto) {

        AtencionMedica atencion = atencionMedicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atención médica no encontrada"));

        validarCamposBase(dto);

        if (dto.getEstado() == EstadoAtencion.COMPLETADA) {
            validarCamposCompletados(dto);
        }

        validarCitaSegunTipoAtencion(dto);

        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        if (!paciente.getEstado()) {
            throw new RuntimeException("No se puede editar atención para un paciente dado de baja");
        }

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Cita cita = null;

        if (dto.getCitaId() != null) {
            cita = citaRepository.findById(dto.getCitaId())
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

            if (atencionMedicaRepository.existsByCitaId(dto.getCitaId())) {
                throw new RuntimeException("Esta cita ya tiene una atención médica registrada");
            }

            if (!cita.getPaciente().getId().equals(dto.getPacienteId())) {
                throw new RuntimeException("La cita no pertenece al paciente indicado");
            }

            if (!cita.getMedico().getId().equals(dto.getUsuarioId())) {
                throw new RuntimeException("La atención debe ser registrada por el médico asignado a la cita");
            }

            if (cita.getEstado() == EstadoCita.CANCELADA || cita.getEstado() == EstadoCita.NO_ASISTIO) {
                throw new RuntimeException("No se puede registrar atención para una cita cancelada o marcada como no asistió");
            }
        }
        atencion.setPaciente(paciente);
        atencion.setUsuario(usuario);
        atencion.setCita(cita);
        atencion.setFechaHora(dto.getFechaHora());
        atencion.setTipoAtencion(dto.getTipoAtencion());
        atencion.setMotivoConsulta(dto.getMotivoConsulta());
        atencion.setPresionArterial(dto.getPresionArterial());
        atencion.setTemperatura(dto.getTemperatura());
        atencion.setSaturacion(dto.getSaturacion());
        atencion.setTalla(dto.getTalla());
        atencion.setPeso(dto.getPeso());
        atencion.setDiagnostico(dto.getDiagnostico());
        atencion.setObservaciones(dto.getObservaciones());
        atencion.setTratamientoIndicado(dto.getTratamientoIndicado());
        atencion.setMedicamentos(dto.getMedicamentos());
        atencion.setEstado(dto.getEstado());

        if (cita != null && dto.getEstado() == EstadoAtencion.COMPLETADA) {
            cita.setEstado(EstadoCita.ATENDIDA);
            citaRepository.save(cita);
        }

        AtencionMedica guardada = atencionMedicaRepository.save(atencion);
        guardada.setHashBlockchain(hashService.generarHashAtencion(guardada));
        guardada = atencionMedicaRepository.save(guardada);
        traceabilityService.registrarEntidad("ATENCION", "ACTUALIZAR", guardada, usuario.getId());

        return convertirADTO(guardada);
    }

    private void validarCitaSegunTipoAtencion(AtencionMedicaRequestDTO dto) {

        if (dto.getTipoAtencion() == TipoAtencion.EMERGENCIA && dto.getCitaId() != null) {
            throw new RuntimeException("Una atención de emergencia no debe estar asociada a una cita");
        }

        if ((dto.getTipoAtencion() == TipoAtencion.CONSULTA ||
                dto.getTipoAtencion() == TipoAtencion.PREVENTIVA)
                && dto.getCitaId() == null) {
            throw new RuntimeException("Las atenciones de tipo consulta o preventiva deben estar asociadas a una cita");
        }
    }

}
