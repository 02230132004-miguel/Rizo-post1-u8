package com.example.auditoria.adapter.out.persistence;

import com.example.auditoria.usecase.port.ConteoCategoria;
import com.example.auditoria.usecase.port.PromedioCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad HallazgoJpaEntity con consultas de proyección optimizadas.
 */
@Repository
public interface HallazgoJpaRepository extends JpaRepository<HallazgoJpaEntity, String> {

    @Query("SELECT new com.example.auditoria.usecase.port.ConteoCategoria(CAST(h.severidad AS string), COUNT(h)) " +
           "FROM HallazgoJpaEntity h " +
           "GROUP BY h.severidad")
    List<ConteoCategoria> contarPorSeveridad();

    @Query("SELECT new com.example.auditoria.usecase.port.ConteoCategoria(CAST(h.estado AS string), COUNT(h)) " +
           "FROM HallazgoJpaEntity h " +
           "GROUP BY h.estado")
    List<ConteoCategoria> contarPorEstado();

    @Query(value = "SELECT area_responsable AS area, " +
                   "COALESCE(AVG(CAST(DATEDIFF('DAY', fecha_deteccion, fecha_cierre) AS DOUBLE)), 0.0) AS promedio " +
                   "FROM hallazgos " +
                   "WHERE estado = 'CERRADO' AND fecha_cierre IS NOT NULL " +
                   "GROUP BY area_responsable",
           nativeQuery = true)
    List<Object[]> promedioDiasCierrePorAreaRaw();
}
