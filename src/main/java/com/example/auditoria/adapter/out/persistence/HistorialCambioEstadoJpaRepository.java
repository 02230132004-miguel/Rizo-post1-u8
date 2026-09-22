package com.example.auditoria.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la bitácora histórica append-only.
 */
@Repository
public interface HistorialCambioEstadoJpaRepository extends JpaRepository<HistorialCambioEstadoJpaEntity, Long> {

    List<HistorialCambioEstadoJpaEntity> findByHallazgoIdOrderByFechaAsc(String hallazgoId);
}
