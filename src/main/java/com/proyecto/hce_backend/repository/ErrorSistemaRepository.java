package com.proyecto.hce_backend.repository;

import com.proyecto.hce_backend.model.ErrorSistema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorSistemaRepository extends JpaRepository<ErrorSistema, Long> {
}