package com.proyecto.hce_backend.dto;

import com.proyecto.hce_backend.model.EstadoAtencion;
import com.proyecto.hce_backend.model.TipoAtencion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtencionMedicaDTO {

    private Long id;

    private Long pacienteId;
    private String pacienteNombre;

    private Long usuarioId;
    private String profesionalSalud;

    private LocalDateTime fechaHora;
    private TipoAtencion tipoAtencion;
    private String motivoConsulta;

    private String presionArterial;
    private Double temperatura;
    private Integer saturacion;
    private Double talla;
    private Double peso;

    private String diagnostico;
    private String observaciones;
    private String tratamientoIndicado;
    private String medicamentos;

    private EstadoAtencion estado;

    private Long citaId;
}