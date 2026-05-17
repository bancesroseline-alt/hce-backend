package com.proyecto.hce_backend.repository;

import com.proyecto.hce_backend.model.TrazabilidadBlockchain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrazabilidadBlockchainRepository extends JpaRepository<TrazabilidadBlockchain, Long> {

    List<TrazabilidadBlockchain> findByAtencionId(Long atencionId);
}
