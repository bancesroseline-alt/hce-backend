package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.CitaDTO;
import com.proyecto.hce_backend.dto.CitaRequestDTO;
import com.proyecto.hce_backend.model.Cita;
import com.proyecto.hce_backend.model.EstadoCita;
import com.proyecto.hce_backend.model.Paciente;
import com.proyecto.hce_backend.model.Usuario;
import com.proyecto.hce_backend.repository.CitaRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import com.proyecto.hce_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BlockchainTraceabilityService traceabilityService;

    public CitaDTO registrarCita(CitaRequestDTO dto) {

        validarCampos(dto);

        if (citaRepository.existsByMedicoIdAndFechaAndHora(dto.getMedicoId(), dto.getFecha(), dto.getHora())) {
            throw new RuntimeException("El médico ya tiene una cita registrada en esa fecha y hora");
        }

        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Usuario medico = usuarioRepository.findById(dto.getMedicoId())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        if (!esProfesionalCita(medico.getRol())) {
            throw new RuntimeException("El usuario seleccionado no tiene rol MEDICO o ENFERMERO");
        }

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setTipoCita(dto.getTipoCita());
        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());
        cita.setEspecialidad(normalizarEspecialidad(dto.getEspecialidad()));
        cita.setMotivoConsulta(dto.getMotivoConsulta());
        cita.setEstado(dto.getEstado() != null ? dto.getEstado() : EstadoCita.PROGRAMADA);

        if (!paciente.getEstado()) {
            throw new RuntimeException("No se puede registrar una cita para un paciente dado de baja");
        }

        Cita guardada = citaRepository.save(cita);
        traceabilityService.registrarEntidad("CITA", "CREAR", guardada, medico.getId());

        return convertirADTO(guardada);
    }

    public List<CitaDTO> listarCitas() {
        return citaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<CitaDTO> listarPorPaciente(Long pacienteId) {
        List<Cita> citas = citaRepository.findByPacienteId(pacienteId);

        return citas.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<CitaDTO> listarPorMedico(Long medicoId) {
        return citaRepository.findByMedicoId(medicoId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public CitaDTO actualizarEstado(Long citaId, EstadoCita estado) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        cita.setEstado(estado);

        Cita guardada = citaRepository.save(cita);
        Long usuarioId = guardada.getMedico() != null ? guardada.getMedico().getId() : null;
        traceabilityService.registrarEntidad("CITA", "ACTUALIZAR", guardada, usuarioId);

        return convertirADTO(guardada);
    }

    private void validarCampos(CitaRequestDTO dto) {
        if (dto.getPacienteId() == null) throw new RuntimeException("El paciente es obligatorio");
        if (dto.getMedicoId() == null) throw new RuntimeException("El profesional es obligatorio");
        if (dto.getTipoCita() == null) throw new RuntimeException("El tipo de cita es obligatorio");
        if (dto.getFecha() == null) throw new RuntimeException("La fecha es obligatoria");
        if (dto.getHora() == null) throw new RuntimeException("La hora es obligatoria");

        if (dto.getEspecialidad() == null || dto.getEspecialidad().isBlank()) {
            throw new RuntimeException("La especialidad es obligatoria");
        }

        if (dto.getMotivoConsulta() == null || dto.getMotivoConsulta().isBlank()) {
            throw new RuntimeException("El motivo de consulta es obligatorio");
        }
    }

    public CitaDTO convertirADTO(Cita cita) {
        return new CitaDTO(
                cita.getId(),
                cita.getPaciente().getId(),
                cita.getPaciente().getNombres() + " " + cita.getPaciente().getApellidos(),
                cita.getMedico().getId(),
                cita.getMedico().getNombres() + " " + cita.getMedico().getApellidos(),
                cita.getTipoCita(),
                cita.getFecha(),
                cita.getHora(),
                cita.getEspecialidad(),
                cita.getMotivoConsulta(),
                cita.getEstado()
        );
    }

    public List<CitaDTO> listarCitasProgramadas() {
        return citaRepository.findByEstado(EstadoCita.PROGRAMADA)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public CitaDTO editarCita(Long id, CitaRequestDTO dto) {

        validarCampos(dto);

        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (!cita.getFecha().equals(dto.getFecha()) || !cita.getHora().equals(dto.getHora())
                || !cita.getMedico().getId().equals(dto.getMedicoId())) {

            if (citaRepository.existsByMedicoIdAndFechaAndHora(dto.getMedicoId(), dto.getFecha(), dto.getHora())) {
                throw new RuntimeException("El médico ya tiene una cita registrada en esa fecha y hora");
            }
        }

        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Usuario medico = usuarioRepository.findById(dto.getMedicoId())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        if (!esProfesionalCita(medico.getRol())) {
            throw new RuntimeException("El usuario seleccionado no tiene rol MEDICO o ENFERMERO");
        }

        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setTipoCita(dto.getTipoCita());
        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());
        cita.setEspecialidad(normalizarEspecialidad(dto.getEspecialidad()));
        cita.setMotivoConsulta(dto.getMotivoConsulta());
        cita.setEstado(dto.getEstado() != null ? dto.getEstado() : cita.getEstado());

        Cita guardada = citaRepository.save(cita);
        traceabilityService.registrarEntidad("CITA", "ACTUALIZAR", guardada, medico.getId());

        return convertirADTO(guardada);
    }

    public Long totalCitas() {
        return citaRepository.count();
    }

    public Long citasHoy() {
        LocalDate hoy = LocalDate.now();
        return citaRepository.countByFecha(hoy);
    }

    private boolean esProfesionalCita(String rol) {
        String rolNormalizado = rol == null ? "" : rol.replace("ROLE_", "").toUpperCase();
        return "MEDICO".equals(rolNormalizado) || "ENFERMERO".equals(rolNormalizado);
    }

    private String normalizarEspecialidad(String valor) {
        if (valor == null) return null;

        String texto = valor.trim().replaceAll("\\s+", " ").toLowerCase(new Locale("es", "PE"));
        if (texto.isBlank()) return texto;

        String[] palabras = texto.split(" ");
        StringBuilder normalizada = new StringBuilder();

        for (String palabra : palabras) {
            if (palabra.isBlank()) continue;
            if (normalizada.length() > 0) normalizada.append(" ");
            normalizada.append(palabra.substring(0, 1).toUpperCase(new Locale("es", "PE")));
            if (palabra.length() > 1) normalizada.append(palabra.substring(1));
        }

        return normalizada.toString();
    }
}
