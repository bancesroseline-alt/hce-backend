package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.MetricaSistemaDTO;
import com.proyecto.hce_backend.repository.ErrorSistemaRepository;
import com.proyecto.hce_backend.repository.FeedbackSistemaRepository;
import com.proyecto.hce_backend.repository.LogSistemaRepository;
import com.proyecto.hce_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.lang.management.ManagementFactory;

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
        long logs = logSistemaRepository.count();
        long errores = errorSistemaRepository.count();
        Runtime runtime = Runtime.getRuntime();
        long usadaMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long disponibleMb = runtime.maxMemory() / (1024 * 1024);
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        double tasaErrores = logs == 0 ? 0.0 : (errores * 100.0) / logs;

        return new MetricaSistemaDTO(
                usuarioRepository.count(),
                logs,
                errores,
                feedbackSistemaRepository.count(),
                usadaMb,
                disponibleMb,
                uptime,
                Math.round(tasaErrores * 100.0) / 100.0
        );
    }
}
