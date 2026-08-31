package com.proyecto.hce_backend.security;

import com.proyecto.hce_backend.model.LogSistema;
import com.proyecto.hce_backend.repository.LogSistemaRepository;
import com.proyecto.hce_backend.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class AuditLoggingFilter extends OncePerRequestFilter {

    private final LogSistemaRepository logRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditLoggingFilter(LogSistemaRepository logRepository, UsuarioRepository usuarioRepository) {
        this.logRepository = logRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long inicio = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (request.getRequestURI().startsWith("/api/") &&
                    !request.getRequestURI().startsWith("/api/logs") &&
                    !"OPTIONS".equalsIgnoreCase(request.getMethod())) {
                registrar(request, response, System.currentTimeMillis() - inicio);
            }
        }
    }

    private void registrar(HttpServletRequest request, HttpServletResponse response, long duracionMs) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long usuarioId = null;
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                usuarioId = usuarioRepository.findByUsername(auth.getName()).map(u -> u.getId()).orElse(null);
            }

            LogSistema log = new LogSistema();
            log.setUsuarioId(usuarioId);
            log.setAccion(request.getMethod() + " " + request.getRequestURI() +
                    " [" + response.getStatus() + ", " + duracionMs + "ms]");
            log.setModulo(obtenerModulo(request.getRequestURI()));
            log.setFechaRegistro(LocalDateTime.now());
            logRepository.save(log);
        } catch (Exception ignored) {
            // La auditoria nunca debe interrumpir la operacion principal.
        }
    }

    private String obtenerModulo(String uri) {
        String[] partes = uri.split("/");
        return partes.length > 2 ? partes[2].toUpperCase() : "SISTEMA";
    }
}
