package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.dto.AuthResponseDTO;
import com.proyecto.hce_backend.dto.LoginDTO;
import com.proyecto.hce_backend.dto.UsuarioDTO;
import com.proyecto.hce_backend.model.Usuario;
import com.proyecto.hce_backend.security.JwtUtil;
import com.proyecto.hce_backend.service.AuthService;
import com.proyecto.hce_backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginDTO loginDTO) {

        Usuario usuario = authService.login(
                loginDTO.getUsername(),
                loginDTO.getPassword()
        );

        UsuarioDTO usuarioDTO = usuarioService.convertirAUsuarioDTO(usuario);
        String token = jwtUtil.generarToken(usuario);

        return new AuthResponseDTO(token, usuarioDTO);
    }
}