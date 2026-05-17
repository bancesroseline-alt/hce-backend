package com.proyecto.hce_backend.dto;

import com.proyecto.hce_backend.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;
    private String nombres;
    private String apellidos;
    private String username;
    private String rol;
    private Boolean estado;
}
