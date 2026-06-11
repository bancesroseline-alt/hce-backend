package com.proyecto.hce_backend.dto;

import lombok.Data;

@Data
public class FastApiResponseDTO {

    private Integer prediccion;
    private Double probabilidad;
}
