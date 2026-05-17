package com.proyecto.hce_backend.dto;

import com.proyecto.hce_backend.model.EstadoCita;
import com.proyecto.hce_backend.model.TipoCita;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaDTO {

    private Long id;

    private Long pacienteId;
    private String pacienteNombre;

    private Long medicoId;
    private String medicoNombre;

    private TipoCita tipoCita;
    private LocalDate fecha;
    private LocalTime hora;
    private String especialidad;
    private String motivoConsulta;
    private EstadoCita estado;
}