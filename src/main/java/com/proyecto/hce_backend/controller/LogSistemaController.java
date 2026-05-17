package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.model.LogSistema;
import com.proyecto.hce_backend.repository.LogSistemaRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@SecurityRequirement(name = "bearerAuth")
public class LogSistemaController {

    final LogSistemaRepository logSistemaRepository;

    public LogSistemaController(LogSistemaRepository logSistemaRepository) {
        this.logSistemaRepository = logSistemaRepository;
    }

    @PostMapping
    public LogSistema registrarLog(@RequestBody LogSistema logSistema) {
        logSistema.setFechaRegistro(LocalDateTime.now());
        return logSistemaRepository.save(logSistema);
    }

    @GetMapping
    public List<LogSistema> listarLogs() {
        return logSistemaRepository.findAll();
    }
}