package com.proyecto.hce_backend.repository;

import com.proyecto.hce_backend.model.TrazabilidadBlockchain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrazabilidadBlockchainRepository extends JpaRepository<TrazabilidadBlockchain, Long> {

    List<TrazabilidadBlockchain> findByAtencionId(Long atencionId);

    List<TrazabilidadBlockchain> findAllByOrderByFechaRegistroDesc();

    List<TrazabilidadBlockchain> findByEntityTypeAndEntityIdOrderByFechaRegistroDesc(String entityType, String entityId);

    Optional<TrazabilidadBlockchain> findTopByEntityTypeAndEntityIdOrderByFechaRegistroDesc(String entityType, String entityId);
}
