package com.proyecto.hce_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_sistema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId;

    @Column(nullable = false, length = 1000)
    private String comentario;

    private Integer calificacion;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;
}