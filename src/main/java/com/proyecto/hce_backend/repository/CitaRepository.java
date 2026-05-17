package com.proyecto.hce_backend.repository;

import com.proyecto.hce_backend.model.Cita;
import com.proyecto.hce_backend.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByPacienteId(Long pacienteId);

    List<Cita> findByMedicoId(Long medicoId);

    Optional<Cita> findByUuidLocal(String uuidLocal);
    List<Cita> findByEstado(EstadoCita estado);

    boolean existsByMedicoIdAndFechaAndHora(Long medicoId, LocalDate fecha, LocalTime hora);
    long countByFecha(LocalDate fecha);
}