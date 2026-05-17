package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.model.ErrorSistema;
import com.proyecto.hce_backend.repository.ErrorSistemaRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/errores")
@SecurityRequirement(name = "bearerAuth")
public class ErrorSistemaController {

    final ErrorSistemaRepository errorSistemaRepository;

    public ErrorSistemaController(ErrorSistemaRepository errorSistemaRepository) {
        this.errorSistemaRepository = errorSistemaRepository;
    }

    @PostMapping
    public ErrorSistema registrarError(@RequestBody ErrorSistema errorSistema) {
        errorSistema.setFechaRegistro(LocalDateTime.now());
        return errorSistemaRepository.save(errorSistema);
    }

    @GetMapping
    public List<ErrorSistema> listarErrores() {
        return errorSistemaRepository.findAll();
    }
}