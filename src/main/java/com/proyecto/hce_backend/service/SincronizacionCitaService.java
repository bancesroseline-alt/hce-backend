package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.SincronizacionCitaRequestDTO;
import com.proyecto.hce_backend.model.*;
import com.proyecto.hce_backend.repository.CitaRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import com.proyecto.hce_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class SincronizacionCitaService {

    final CitaRepository citaRepository;
    final PacienteRepository pacienteRepository;
    final UsuarioRepository usuarioRepository;
    final BlockchainTraceabilityService traceabilityService;

    public SincronizacionCitaService(CitaRepository citaRepository,
                                     PacienteRepository pacienteRepository,
                                     UsuarioRepository usuarioRepository,
                                     BlockchainTraceabilityService traceabilityService) {
        this.citaRepository = citaRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.traceabilityService = traceabilityService;
    }

    public Cita sincronizarCita(SincronizacionCitaRequestDTO dto) {

        if (dto.getUuidLocal() != null &&
                citaRepository.findByUuidLocal(dto.getUuidLocal()).isPresent()) {
            Cita existente = citaRepository.findByUuidLocal(dto.getUuidLocal()).get();
            actualizarDatosCita(existente, dto);
            existente.setFechaSincronizacion(LocalDateTime.now());
            existente.setSincronizado(true);
            Cita guardada = citaRepository.save(existente);
            Long usuarioId = guardada.getMedico() != null ? guardada.getMedico().getId() : null;
            traceabilityService.registrarEntidad("CITA", "SINCRONIZAR", guardada, usuarioId);
            return guardada;
        }

        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Usuario medico = usuarioRepository.findById(dto.getMedicoId())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        Cita cita = new Cita();

        cita.setPaciente(paciente);
        cita.setMedico(medico);
        actualizarDatosCita(cita, dto);

        cita.setUuidLocal(dto.getUuidLocal());
        cita.setOrigenRegistro("INDEXEDDB");
        cita.setSincronizado(true);
        cita.setFechaSincronizacion(LocalDateTime.now());

        Cita guardada = citaRepository.save(cita);
        traceabilityService.registrarEntidad("CITA", "SINCRONIZAR", guardada, medico.getId());

        return guardada;
    }

    private void actualizarDatosCita(Cita cita, SincronizacionCitaRequestDTO dto) {
        cita.setTipoCita(TipoCita.valueOf(dto.getTipoCita()));
        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());
        cita.setEspecialidad(normalizarEspecialidad(dto.getEspecialidad()));
        cita.setMotivoConsulta(dto.getMotivoConsulta());
        cita.setEstado(EstadoCita.valueOf(dto.getEstado()));
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
