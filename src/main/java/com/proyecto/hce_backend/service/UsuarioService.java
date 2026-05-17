package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.UsuarioDTO;
import com.proyecto.hce_backend.model.Usuario;
import com.proyecto.hce_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public UsuarioDTO convertirAUsuarioDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getUsername(),
                usuario.getRol(),
                usuario.getEstado()
        );
    }

    public UsuarioDTO crearUsuario(Usuario usuario) {
        if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
            throw new RuntimeException("El username ya existe");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return convertirAUsuarioDTO(usuarioGuardado);
    }

    public UsuarioDTO actualizarRol(Long id, String rol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!rol.equals("ADMIN") && !rol.equals("MEDICO") && !rol.equals("ENFERMERO")) {
            throw new RuntimeException("Rol no válido");
        }

        usuario.setRol(rol);

        Usuario actualizado = usuarioRepository.save(usuario);

        return convertirAUsuarioDTO(actualizado);
    }

    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirAUsuarioDTO)
                .toList();
    }
}