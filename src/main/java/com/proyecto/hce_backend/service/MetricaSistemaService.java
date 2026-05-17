package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.MetricaSistemaDTO;
import com.proyecto.hce_backend.repository.ErrorSistemaRepository;
import com.proyecto.hce_backend.repository.FeedbackSistemaRepository;
import com.proyecto.hce_backend.repository.LogSistemaRepository;
import com.proyecto.hce_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class MetricaSistemaService {

    final UsuarioRepository usuarioRepository;
    final LogSistemaRepository logSistemaRepository;
    final ErrorSistemaRepository errorSistemaRepository;
    final FeedbackSistemaRepository feedbackSistemaRepository;

    public MetricaSistemaService(UsuarioRepository usuarioRepository,
                                 LogSistemaRepository logSistemaRepository,
                                 ErrorSistemaRepository errorSistemaRepository,
                                 FeedbackSistemaRepository feedbackSistemaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.logSistemaRepository = logSistemaRepository;
        this.errorSistemaRepository = errorSistemaRepository;
        this.feedbackSistemaRepository = feedbackSistemaRepository;
    }

    public MetricaSistemaDTO obtenerMetricas() {
        return new MetricaSistemaDTO(
                usuarioRepository.count(),
                logSistemaRepository.count(),
                errorSistemaRepository.count(),
                feedbackSistemaRepository.count()
        );
    }
}