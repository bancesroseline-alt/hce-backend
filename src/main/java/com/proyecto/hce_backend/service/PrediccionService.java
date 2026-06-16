package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.FastApiResponseDTO;
import com.proyecto.hce_backend.dto.PrediccionPacienteDTO;
import com.proyecto.hce_backend.dto.PrediccionRequestDTO;
import com.proyecto.hce_backend.dto.PrediccionResponseDTO;
import com.proyecto.hce_backend.model.Cita;
import com.proyecto.hce_backend.model.Paciente;
import com.proyecto.hce_backend.model.PrediccionInasistencia;
import com.proyecto.hce_backend.repository.CitaRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import com.proyecto.hce_backend.repository.PrediccionInasistenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrediccionService {

private final RestTemplate restTemplate;
private final PacienteRepository pacienteRepository;
private final CitaRepository citaRepository;
private final PrediccionInasistenciaRepository prediccionRepository;

@Value("${ml.api.url}")
private String mlApiUrl;

public PrediccionService(
        PacienteRepository pacienteRepository,
        CitaRepository citaRepository,
        PrediccionInasistenciaRepository prediccionRepository,
        RestTemplate restTemplate) {

    this.pacienteRepository = pacienteRepository;
    this.citaRepository = citaRepository;
    this.prediccionRepository = prediccionRepository;
    this.restTemplate = restTemplate;
}

public PrediccionResponseDTO predecirInasistencia(PrediccionRequestDTO dto) {

    Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

    Cita cita = null;

    if (dto.getCitaId() != null) {
        cita = citaRepository.findById(dto.getCitaId())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
    }

    Map<String, Object> request = new HashMap<>();

    request.put("edad", dto.getEdad() != null ? dto.getEdad() : paciente.getEdad());

    request.put(
            "sexo",
            paciente.getSexo()
    );

    request.put(
            "tipo_cita",
            dto.getTipoCita()
    );

    request.put(
            "especialidad",
            dto.getEspecialidad()
    );

    request.put(
            "dia_semana",
            dto.getDiaSemana() != null && !dto.getDiaSemana().isBlank()
                    ? dto.getDiaSemana()
                    : cita != null
                    ? cita.getFecha().getDayOfWeek().toString()
                    : "MONDAY"
    );

    request.put(
            "hora",
            dto.getHora() != null
                    ? dto.getHora()
                    : cita != null
                    ? cita.getHora().getHour()
                    : 10
    );

    request.put(
            "antecedentes_inasistencias",
            dto.getCantidadInasistenciasPrevias() != null ? dto.getCantidadInasistenciasPrevias() : 0
    );

    request.put(
            "cantidad_citas_previas",
            dto.getCantidadCitasPrevias() != null ? dto.getCantidadCitasPrevias() : 0
    );

    FastApiResponseDTO respuestaIA =
        restTemplate.postForObject(
                mlApiUrl,
                request,
                FastApiResponseDTO.class
        );

    if (respuestaIA == null || respuestaIA.getProbabilidad() == null) {
        throw new RuntimeException("La API ML no devolvio una probabilidad valida");
    }

    double probabilidad = respuestaIA.getProbabilidad();

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

public List<PrediccionPacienteDTO> listarPrediccionesPacientes() {
    return pacienteRepository.findAll()
            .stream()
            .map(this::predecirPaciente)
            .toList();
}

private PrediccionPacienteDTO predecirPaciente(Paciente paciente) {
    List<Cita> citas = citaRepository.findByPacienteId(paciente.getId());
    Cita citaReferencia = citas.stream()
            .filter(c -> c.getEstado() == com.proyecto.hce_backend.model.EstadoCita.PROGRAMADA)
            .max(Comparator.comparing(Cita::getFecha).thenComparing(Cita::getHora))
            .orElseGet(() -> citas.stream()
                    .max(Comparator.comparing(Cita::getFecha).thenComparing(Cita::getHora))
                    .orElse(null));

    int inasistencias = (int) citas.stream()
            .filter(c -> c.getEstado() == com.proyecto.hce_backend.model.EstadoCita.NO_ASISTIO)
            .count();
    int reprogramaciones = (int) citas.stream()
            .filter(c -> c.getEstado() == com.proyecto.hce_backend.model.EstadoCita.NO_ASISTIO
                    || c.getEstado() == com.proyecto.hce_backend.model.EstadoCita.CANCELADA)
            .count();

    PrediccionRequestDTO request = new PrediccionRequestDTO();
    request.setPacienteId(paciente.getId());
    request.setEdad(paciente.getEdad());
    request.setCantidadCitasPrevias(citas.size());
    request.setCantidadInasistenciasPrevias(inasistencias);
    request.setTipoCita(citaReferencia != null ? citaReferencia.getTipoCita().name() : "CONSULTA");
    request.setEspecialidad(citaReferencia != null ? citaReferencia.getEspecialidad() : "Medicina General");
    request.setCitaId(citaReferencia != null ? citaReferencia.getId() : null);
    request.setDiaSemana(citaReferencia != null ? citaReferencia.getFecha().getDayOfWeek().toString() : "MONDAY");
    request.setHora(citaReferencia != null ? citaReferencia.getHora().getHour() : 10);

    PrediccionResponseDTO prediccion;

    try {
        prediccion = predecirInasistencia(request);
    } catch (Exception error) {
        prediccion = obtenerPrediccionRespaldo(paciente, citas, inasistencias, reprogramaciones);
    }

    return new PrediccionPacienteDTO(
            paciente.getId(),
            paciente.getNombres(),
            paciente.getApellidos(),
            prediccion.getProbabilidadInasistencia(),
            prediccion.getNivelRiesgo(),
            prediccion.getRecomendacion(),
            citas.size(),
            inasistencias,
            reprogramaciones
    );
}

private PrediccionResponseDTO obtenerPrediccionRespaldo(
        Paciente paciente,
        List<Cita> citas,
        int inasistencias,
        int reprogramaciones) {

    return prediccionRepository.findTopByPacienteIdOrderByFechaPrediccionDesc(paciente.getId())
            .map(prediccion -> new PrediccionResponseDTO(
                    paciente.getId(),
                    prediccion.getProbabilidadInasistencia(),
                    prediccion.getNivelRiesgo(),
                    (prediccion.getRecomendacion() != null
                            ? prediccion.getRecomendacion()
                            : "Ultimo resultado guardado") + " (ultimo resultado guardado)"
            ))
            .orElseGet(() -> calcularPrediccionLocal(paciente, citas, inasistencias, reprogramaciones));
}

private PrediccionResponseDTO calcularPrediccionLocal(
        Paciente paciente,
        List<Cita> citas,
        int inasistencias,
        int reprogramaciones) {

    double probabilidad = 0.0;

    if (!citas.isEmpty()) {
        probabilidad = (double) inasistencias / citas.size();
        probabilidad += Math.min(reprogramaciones * 0.08, 0.24);
    }

    probabilidad = Math.max(0.0, Math.min(probabilidad, 1.0));

    String nivelRiesgo;
    String recomendacion;

    if (probabilidad >= 0.70) {
        nivelRiesgo = "ALTO";
        recomendacion = "Seguimiento inmediato (estimacion local)";
    } else if (probabilidad >= 0.40) {
        nivelRiesgo = "MEDIO";
        recomendacion = "Monitoreo preventivo (estimacion local)";
    } else {
        nivelRiesgo = "BAJO";
        recomendacion = "Sin accion (estimacion local)";
    }

    return new PrediccionResponseDTO(
            paciente.getId(),
            probabilidad,
            nivelRiesgo,
            recomendacion
    );
}

}
