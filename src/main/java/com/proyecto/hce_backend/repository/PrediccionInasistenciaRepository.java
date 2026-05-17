package com.proyecto.hce_backend.repository;

import com.proyecto.hce_backend.model.PrediccionInasistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrediccionInasistenciaRepository extends JpaRepository<PrediccionInasistencia, Long> {

    List<PrediccionInasistencia> findByPacienteId(Long pacienteId);

    List<PrediccionInasistencia> findByNivelRiesgo(String nivelRiesgo);
}