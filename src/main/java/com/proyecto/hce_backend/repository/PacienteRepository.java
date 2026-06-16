package com.proyecto.hce_backend.repository;

import com.proyecto.hce_backend.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByNumeroDocumento(String numeroDocumento);

    Optional<Paciente> findByUuidLocal(String uuidLocal);

    List<Paciente> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrNumeroDocumentoContainingIgnoreCase(
            String nombres,
            String apellidos,
            String numeroDocumento
    );

    boolean existsByNumeroDocumento(String numeroDocumento);
}
