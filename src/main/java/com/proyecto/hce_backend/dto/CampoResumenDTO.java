package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Par etiqueta/valor para mostrar los datos clinicos de un registro. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampoResumenDTO {

    private String etiqueta;
    private String valor;
}
