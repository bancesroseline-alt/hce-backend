package com.proyecto.hce_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "predicciones_inasistencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrediccionInasistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @Column(nullable = false)
    private Double probabilidadInasistencia;

    @Column(nullable = false)
    private String nivelRiesgo;

    @Column(length = 1000)
    private String recomendacion;

    @Column(nullable = false)
    private LocalDateTime fechaPrediccion;
}