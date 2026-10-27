package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cambio de un dato clinico entre dos versiones consecutivas de un registro. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioCampoDTO {

    private String etiqueta;
    private String antes;
    private String despues;
}
