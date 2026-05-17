package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.AtencionMedicaDTO;
import com.proyecto.hce_backend.dto.HistoriaClinicaDTO;
import com.proyecto.hce_backend.dto.PacienteDTO;
import com.proyecto.hce_backend.model.Paciente;
import com.proyecto.hce_backend.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoriaClinicaService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private AtencionMedicaService atencionMedicaService;

    public HistoriaClinicaDTO obtenerHistoriaClinicaPorPaciente(Long pacienteId) {

        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        PacienteDTO pacienteDTO = pacienteService.convertirAPacienteDTO(paciente);

        List<AtencionMedicaDTO> atenciones = atencionMedicaService.listarPorPaciente(pacienteId);

        return new HistoriaClinicaDTO(pacienteDTO, atenciones);
    }
}