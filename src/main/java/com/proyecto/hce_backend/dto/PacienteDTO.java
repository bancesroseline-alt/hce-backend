package com.proyecto.hce_backend.dto;

import com.proyecto.hce_backend.model.EstadoCivil;
import com.proyecto.hce_backend.model.GrupoSanguineo;
import com.proyecto.hce_backend.model.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PacienteDTO {

    private Long id;

    private TipoDocumento tipoDocumento;
    private String numeroDocumento;

    private String nombres;
    private String apellidos;

    private String fechaNacimiento;
    private Integer edad;

    private String sexo;
    private EstadoCivil estadoCivil;

    private String telefono;
    private String direccion;

    private String antecedentes;

    private Boolean estado;

    private String uuidLocal;
}
