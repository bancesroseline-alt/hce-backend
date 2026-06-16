package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.SincronizacionCitaRequestDTO;
import com.proyecto.hce_backend.model.*;
import com.proyecto.hce_backend.repository.CitaRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import com.proyecto.hce_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SincronizacionCitaService {

    final CitaRepository citaRepository;
    final PacienteRepository pacienteRepository;
    final UsuarioRepository usuarioRepository;

    public SincronizacionCitaService(CitaRepository citaRepository,
                                     PacienteRepository pacienteRepository,
                                     UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Cita sincronizarCita(SincronizacionCitaRequestDTO dto) {

        if (dto.getUuidLocal() != null &&
                citaRepository.findByUuidLocal(dto.getUuidLocal()).isPresent()) {
            Cita existente = citaRepository.findByUuidLocal(dto.getUuidLocal()).get();
            actualizarDatosCita(existente, dto);
            existente.setFechaSincronizacion(LocalDateTime.now());
            existente.setSincronizado(true);
            return citaRepository.save(existente);
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

        return citaRepository.save(cita);
    }

    private void actualizarDatosCita(Cita cita, SincronizacionCitaRequestDTO dto) {
        cita.setTipoCita(TipoCita.valueOf(dto.getTipoCita()));
        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());
        cita.setEspecialidad(dto.getEspecialidad());
        cita.setMotivoConsulta(dto.getMotivoConsulta());
        cita.setEstado(EstadoCita.valueOf(dto.getEstado()));
    }
}
