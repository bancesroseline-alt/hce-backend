package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.SincronizacionRequestDTO;
import com.proyecto.hce_backend.model.*;
import com.proyecto.hce_backend.repository.AtencionMedicaRepository;
import com.proyecto.hce_backend.repository.CitaRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import com.proyecto.hce_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SincronizacionService {

    final AtencionMedicaRepository atencionMedicaRepository;
    final PacienteRepository pacienteRepository;
    final UsuarioRepository usuarioRepository;
    final CitaRepository citaRepository;
    final BlockchainService blockchainService;

    public SincronizacionService(AtencionMedicaRepository atencionMedicaRepository,
                                 PacienteRepository pacienteRepository,
                                 UsuarioRepository usuarioRepository,
                                 CitaRepository citaRepository,
                                 BlockchainService blockchainService) {
        this.atencionMedicaRepository = atencionMedicaRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.citaRepository = citaRepository;
        this.blockchainService = blockchainService;
    }

    public AtencionMedica sincronizarAtencion(SincronizacionRequestDTO dto) {

        if (dto.getUuidLocal() != null &&
                atencionMedicaRepository.findByUuidLocal(dto.getUuidLocal()).isPresent()) {
            throw new RuntimeException("El registro ya fue sincronizado anteriormente");
        }

        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        //if (!paciente.getEstado()) {
            //throw new RuntimeException("No se puede sincronizar atención de un paciente dado de baja");
        //}

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

        atencion.setTipoAtencion(TipoAtencion.valueOf(dto.getTipoAtencion()));
        atencion.setMotivoConsulta(dto.getMotivoConsulta());
        atencion.setDiagnostico(dto.getDiagnostico());
        atencion.setTratamientoIndicado(dto.getTratamientoIndicado());
        atencion.setObservaciones(dto.getObservaciones());
        atencion.setEstado(EstadoAtencion.valueOf(dto.getEstado()));

        atencion.setUuidLocal(dto.getUuidLocal());
        atencion.setOrigenRegistro("INDEXEDDB");
        atencion.setSincronizado(true);
        atencion.setFechaSincronizacion(LocalDateTime.now());

        AtencionMedica guardada = atencionMedicaRepository.save(atencion);

        String hash = blockchainService.generarHashAtencion(guardada);
        guardada.setHashBlockchain(hash);

        return atencionMedicaRepository.save(guardada);
    }
}