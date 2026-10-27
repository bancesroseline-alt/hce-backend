package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEstadoDTO {

    private String modo;
    private String canal;
    private String contrato;
    private List<String> organizaciones;
    private String orderers;
    private String politicaEndoso;
    private long totalBloques;
    private Boolean cadenaIntegra;
    private Long primerBloqueInvalido;
    private Long ultimoBloque;
    private String ultimoHash;
}
