package com.proyecto.hce_backend.repository;

import com.proyecto.hce_backend.model.AtencionMedica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtencionMedicaRepository extends JpaRepository<AtencionMedica, Long> {

    List<AtencionMedica> findByPacienteId(Long pacienteId);
    boolean existsByCitaId(Long citaId);
    Optional<AtencionMedica> findByUuidLocal(String uuidLocal);
}

