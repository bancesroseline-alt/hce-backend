package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.model.FeedbackSistema;
import com.proyecto.hce_backend.repository.FeedbackSistemaRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@SecurityRequirement(name = "bearerAuth")
public class FeedbackSistemaController {

    final FeedbackSistemaRepository feedbackSistemaRepository;

    public FeedbackSistemaController(FeedbackSistemaRepository feedbackSistemaRepository) {
        this.feedbackSistemaRepository = feedbackSistemaRepository;
    }

    @PostMapping
    public FeedbackSistema registrarFeedback(@RequestBody FeedbackSistema feedbackSistema) {
        feedbackSistema.setFechaRegistro(LocalDateTime.now());
        return feedbackSistemaRepository.save(feedbackSistema);
    }

    @GetMapping
    public List<FeedbackSistema> listarFeedback() {
        return feedbackSistemaRepository.findAll();
    }
}