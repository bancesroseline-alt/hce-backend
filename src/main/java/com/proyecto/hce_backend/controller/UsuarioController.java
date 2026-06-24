package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.dto.UsuarioDTO;
import com.proyecto.hce_backend.model.Usuario;
import com.proyecto.hce_backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public UsuarioDTO crearUsuario(@RequestBody Usuario usuario) {
        return usuarioService.crearUsuario(usuario);
    }

    @GetMapping
    public List<UsuarioDTO > listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @PutMapping("/{id}/rol")
    public UsuarioDTO actualizarRol(@PathVariable Long id, @RequestParam String rol) {
        return usuarioService.actualizarRol(id, rol);
    }

    @PutMapping("/{id}")
    public UsuarioDTO actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        return usuarioService.actualizarUsuario(id, usuario);
    }

    @PatchMapping("/{id}/estado")
    public UsuarioDTO actualizarEstado(@PathVariable Long id, @RequestParam Boolean estado) {
        return usuarioService.actualizarEstado(id, estado);
    }

    @DeleteMapping("/{id}")
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }
}
