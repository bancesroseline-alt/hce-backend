package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriaClinicaDTO {

    private PacienteDTO paciente;
    private List<AtencionMedicaDTO> atenciones;
}