package com.proyecto.hce_backend.repository;

import com.proyecto.hce_backend.model.LedgerBloque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LedgerBloqueRepository extends JpaRepository<LedgerBloque, Long> {

    Optional<LedgerBloque> findByOperationId(String operationId);

    Optional<LedgerBloque> findTopByEntityTypeAndEntityIdOrderByVersionDesc(String entityType, String entityId);

    Optional<LedgerBloque> findByEntityTypeAndEntityIdAndVersion(String entityType, String entityId, Integer version);

    List<LedgerBloque> findByEntityTypeAndEntityIdOrderByVersionAsc(String entityType, String entityId);

    List<LedgerBloque> findAllByOrderByNumeroBloqueAsc();

    Optional<LedgerBloque> findTopByOrderByNumeroBloqueDesc();

    long countByEntityTypeAndEntityId(String entityType, String entityId);
}
