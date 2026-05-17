package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.ReporteGeneralDTO;
import com.proyecto.hce_backend.model.Cita;
import com.proyecto.hce_backend.model.EstadoCita;
import com.proyecto.hce_backend.repository.AtencionMedicaRepository;
import com.proyecto.hce_backend.repository.CitaRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteService {

    final PacienteRepository pacienteRepository;
    final CitaRepository citaRepository;
    final AtencionMedicaRepository atencionMedicaRepository;

    public ReporteService(PacienteRepository pacienteRepository,
                          CitaRepository citaRepository,
                          AtencionMedicaRepository atencionMedicaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
        this.atencionMedicaRepository = atencionMedicaRepository;
    }

    public ReporteGeneralDTO reporteGeneral() {
        Long totalPacientes = pacienteRepository.count();
        Long totalCitas = citaRepository.count();
        Long totalAtenciones = atencionMedicaRepository.count();
        Long totalInasistencias = (long) citaRepository.findByEstado(EstadoCita.NO_ASISTIO).size();

        return new ReporteGeneralDTO(
                totalPacientes,
                totalCitas,
                totalAtenciones,
                totalInasistencias
        );
    }

    public List<Cita> reporteCitas() {
        return citaRepository.findAll();
    }

    public List<Cita> reporteInasistencias() {
        return citaRepository.findByEstado(EstadoCita.NO_ASISTIO);
    }
}