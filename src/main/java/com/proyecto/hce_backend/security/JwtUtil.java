package com.proyecto.hce_backend.security;

import com.proyecto.hce_backend.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "hce_backend_seguridad_jwt_2026_proyecto";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 8;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("rol", normalizarRol(usuario.getRol()))
                .claim("id", usuario.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    private String normalizarRol(String rol) {
        String rolNormalizado = rol == null ? "" : rol.replace("ROLE_", "").toUpperCase();
        return rolNormalizado.equals("ADMINISTRADOR") ? "ADMIN" : rolNormalizado;
    }
}
