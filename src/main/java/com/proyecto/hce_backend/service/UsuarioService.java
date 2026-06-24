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

        validarUsuario(usuario, true);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return convertirAUsuarioDTO(usuarioGuardado);
    }

    public UsuarioDTO actualizarRol(Long id, String rol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String rolNormalizado = normalizarRol(rol);

        if (!rolNormalizado.equals("ADMIN") && !rolNormalizado.equals("MEDICO") && !rolNormalizado.equals("ENFERMERO")) {
            throw new RuntimeException("Rol no valido");
        }

        usuario.setRol(rolNormalizado);

        Usuario actualizado = usuarioRepository.save(usuario);

        return convertirAUsuarioDTO(actualizado);
    }

    public UsuarioDTO actualizarUsuario(Long id, Usuario usuarioActualizado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        validarUsuario(usuarioActualizado, false);

        usuarioRepository.findByUsername(usuarioActualizado.getUsername())
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new RuntimeException("El username ya existe");
                });

        usuario.setNombres(usuarioActualizado.getNombres());
        usuario.setApellidos(usuarioActualizado.getApellidos());
        usuario.setUsername(usuarioActualizado.getUsername());
        usuario.setRol(normalizarRol(usuarioActualizado.getRol()));
        usuario.setEstado(usuarioActualizado.getEstado());

        if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
        }

        return convertirAUsuarioDTO(usuarioRepository.save(usuario));
    }

    public UsuarioDTO actualizarEstado(Long id, Boolean estado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (estado == null) {
            throw new RuntimeException("El estado es obligatorio");
        }

        usuario.setEstado(estado);

        return convertirAUsuarioDTO(usuarioRepository.save(usuario));
    }

    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }

        usuarioRepository.deleteById(id);
    }

    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirAUsuarioDTO)
                .toList();
    }

    private void validarUsuario(Usuario usuario, boolean requierePassword) {
        if (usuario.getNombres() == null || usuario.getNombres().isBlank()) {
            throw new RuntimeException("Nombre obligatorio");
        }

        if (usuario.getApellidos() == null || usuario.getApellidos().isBlank()) {
            throw new RuntimeException("Apellido obligatorio");
        }

        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new RuntimeException("Username obligatorio");
        }

        if (requierePassword && (usuario.getPassword() == null || usuario.getPassword().isBlank())) {
            throw new RuntimeException("Contrasena obligatoria");
        }

        if (usuario.getRol() == null || usuario.getRol().isBlank()) {
            throw new RuntimeException("Rol obligatorio");
        }

        usuario.setRol(normalizarRol(usuario.getRol()));

        if (!usuario.getRol().equals("ADMIN") && !usuario.getRol().equals("MEDICO") && !usuario.getRol().equals("ENFERMERO")) {
            throw new RuntimeException("Rol no valido");
        }

        if (usuario.getEstado() == null) {
            throw new RuntimeException("Estado obligatorio");
        }
    }

    private String normalizarRol(String rol) {
        String rolNormalizado = rol == null ? "" : rol.replace("ROLE_", "").toUpperCase();
        return rolNormalizado.equals("ADMINISTRADOR") ? "ADMIN" : rolNormalizado;
    }
}
