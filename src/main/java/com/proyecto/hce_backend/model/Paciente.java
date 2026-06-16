package com.proyecto.hce_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "pacientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(nullable = false, unique = true)
    private String numeroDocumento;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Transient
    public Integer getEdad() {
        if (fechaNacimiento == null) return null;
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    @Column(nullable = false)
    private String sexo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCivil estadoCivil;

    private String telefono;

    private String direccion;

    @Enumerated(EnumType.STRING)
    private GrupoSanguineo grupoSanguineo;

    @Column(length = 1000)
    private String antecedentes;

    @Column(nullable = false)
    private Boolean estado;

    @Column(nullable = false)
    private Boolean sincronizado = true;

    private LocalDateTime fechaSincronizacion;

    @Column(nullable = false)
    private String origenRegistro = "BACKEND";

    @Column(unique = true)
    private String uuidLocal;
}
