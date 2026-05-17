package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.PrediccionRequestDTO;
import com.proyecto.hce_backend.dto.PrediccionResponseDTO;
import com.proyecto.hce_backend.model.Cita;
import com.proyecto.hce_backend.model.Paciente;
import com.proyecto.hce_backend.model.PrediccionInasistencia;
import com.proyecto.hce_backend.repository.CitaRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import com.proyecto.hce_backend.repository.PrediccionInasistenciaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrediccionService {

    final PacienteRepository pacienteRepository;
    final CitaRepository citaRepository;
    final PrediccionInasistenciaRepository prediccionRepository;

    public PrediccionService(PacienteRepository pacienteRepository,
                             CitaRepository citaRepository,
                             PrediccionInasistenciaRepository prediccionRepository) {
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
        this.prediccionRepository = prediccionRepository;
    }

    public PrediccionResponseDTO predecirInasistencia(PrediccionRequestDTO dto) {

        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Cita cita = null;

        if (dto.getCitaId() != null) {
            cita = citaRepository.findById(dto.getCitaId())
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        }

        double probabilidad = 0.10;

        if (dto.getCantidadInasistenciasPrevias() != null) {
            probabilidad += dto.getCantidadInasistenciasPrevias() * 0.20;
        }

        if (dto.getCantidadCitasPrevias() != null && dto.getCantidadCitasPrevias() > 5) {
            probabilidad += 0.10;
        }

        if (dto.getEdad() != null && dto.getEdad() > 65) {
            probabilidad += 0.10;
        }

        if ("PREVENTIVA".equalsIgnoreCase(dto.getTipoCita())) {
            probabilidad += 0.05;
        }

        if (probabilidad > 1.0) {
            probabilidad = 1.0;
        }

        String nivelRiesgo;
        String recomendacion;

        if (probabilidad >= 0.70) {
            nivelRiesgo = "ALTO";
            recomendacion = "Se recomienda contactar al paciente y considerar reprogramación preventiva.";
        } else if (probabilidad >= 0.40) {
            nivelRiesgo = "MEDIO";
            recomendacion = "Se recomienda enviar recordatorio antes de la cita.";
        } else {
            nivelRiesgo = "BAJO";
            recomendacion = "No se requiere recomendación de reprogramación.";
        }

        PrediccionInasistencia prediccion = new PrediccionInasistencia();
        prediccion.setPaciente(paciente);
        prediccion.setCita(cita);
        prediccion.setProbabilidadInasistencia(probabilidad);
        prediccion.setNivelRiesgo(nivelRiesgo);
        prediccion.setRecomendacion(recomendacion);
        prediccion.setFechaPrediccion(LocalDateTime.now());

        prediccionRepository.save(prediccion);

        return new PrediccionResponseDTO(
                paciente.getId(),
                probabilidad,
                nivelRiesgo,
                recomendacion
        );
    }

    public List<PrediccionInasistencia> listarPorPaciente(Long pacienteId) {
        return prediccionRepository.findByPacienteId(pacienteId);
    }

    public List<PrediccionInasistencia> listarAlertas() {
        return prediccionRepository.findByNivelRiesgo("ALTO");
    }
}