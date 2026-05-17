package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SincronizacionCitaRequestDTO {

    private String uuidLocal;

    private Long pacienteId;
    private Long medicoId;

    private String tipoCita;
    private LocalDate fecha;
    private LocalTime hora;

    private String especialidad;
    private String motivoConsulta;
    private String estado;

    private String origenRegistro;
}