package com.proyecto.hce_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "atenciones_medicas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtencionMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAtencion tipoAtencion;

    @Column(nullable = false, length = 1000)
    private String motivoConsulta;

    private String presionArterial;
    private Double temperatura;
    private Integer saturacion;
    private Double talla;
    private Double peso;

    @Column(length = 1000)
    private String diagnostico;

    @Column(length = 1000)
    private String observaciones;

    @Column(length = 1000)
    private String tratamientoIndicado;

    @Column(length = 1000)
    private String medicamentos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAtencion estado;

    @ManyToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @Column(nullable = false)
    private Boolean sincronizado = true;

    private LocalDateTime fechaSincronizacion;

    @Column(nullable = false)
    private String origenRegistro = "BACKEND";

    @Column(unique = true)
    private String uuidLocal;

    private String hashBlockchain;
}