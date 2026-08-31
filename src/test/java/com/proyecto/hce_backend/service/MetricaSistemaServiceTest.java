package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.MetricaSistemaDTO;
import com.proyecto.hce_backend.repository.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetricaSistemaServiceTest {
    @Test
    void calculaUsoDesempenoYTasaErrores() {
        UsuarioRepository usuarios=mock(UsuarioRepository.class); LogSistemaRepository logs=mock(LogSistemaRepository.class);
        ErrorSistemaRepository errores=mock(ErrorSistemaRepository.class); FeedbackSistemaRepository feedback=mock(FeedbackSistemaRepository.class);
        when(usuarios.count()).thenReturn(12L); when(logs.count()).thenReturn(100L); when(errores.count()).thenReturn(5L); when(feedback.count()).thenReturn(8L);
        MetricaSistemaDTO dto=new MetricaSistemaService(usuarios,logs,errores,feedback).obtenerMetricas();
        assertEquals(12L,dto.getTotalUsuarios()); assertEquals(5.0,dto.getTasaErrores()); assertTrue(dto.getUptimeSegundos()>=0); assertTrue(dto.getMemoriaDisponibleMb()>0);
    }
}
