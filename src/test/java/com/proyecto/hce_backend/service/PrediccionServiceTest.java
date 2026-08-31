package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.FastApiResponseDTO;
import com.proyecto.hce_backend.dto.PrediccionRequestDTO;
import com.proyecto.hce_backend.dto.PrediccionResponseDTO;
import com.proyecto.hce_backend.model.Paciente;
import com.proyecto.hce_backend.repository.CitaRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import com.proyecto.hce_backend.repository.PrediccionInasistenciaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PrediccionServiceTest {
    @Test
    void riesgoBajoNoGeneraRecomendacion() {
        PacienteRepository pacientes = mock(PacienteRepository.class);
        CitaRepository citas = mock(CitaRepository.class);
        PrediccionInasistenciaRepository predicciones = mock(PrediccionInasistenciaRepository.class);
        RestTemplate rest = mock(RestTemplate.class);
        Paciente paciente = new Paciente(); paciente.setId(1L); paciente.setFechaNacimiento(LocalDate.now().minusYears(30)); paciente.setSexo("F");
        when(pacientes.findById(1L)).thenReturn(Optional.of(paciente));
        FastApiResponseDTO ml = new FastApiResponseDTO(); ml.setProbabilidad(0.20);
        when(rest.postForObject(anyString(), any(), eq(FastApiResponseDTO.class))).thenReturn(ml);
        PrediccionService service = new PrediccionService(pacientes, citas, predicciones, rest);
        ReflectionTestUtils.setField(service, "mlApiUrl", "http://ml/predict");
        PrediccionRequestDTO request = new PrediccionRequestDTO(1L,30,0,0,"CONSULTA","General",null,"MONDAY",10);
        PrediccionResponseDTO resultado = service.predecirInasistencia(request);
        assertEquals("BAJO", resultado.getNivelRiesgo());
        assertNull(resultado.getRecomendacion());
        verify(predicciones).save(any());
    }
}
