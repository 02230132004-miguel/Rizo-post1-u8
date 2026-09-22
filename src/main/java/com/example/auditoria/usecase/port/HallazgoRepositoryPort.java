package com.example.auditoria.usecase.port;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia y consultas agregadas del agregado HallazgoAuditoria.
 * Desacopla la lógica de aplicación de los detalles de base de datos.
 */
public interface HallazgoRepositoryPort {

    void guardar(HallazgoAuditoria hallazgo);

    Optional<HallazgoAuditoria> buscarPorId(HallazgoId id);

    List<HallazgoAuditoria> buscarTodos();

    List<ConteoCategoria> contarPorSeveridad();

    List<ConteoCategoria> contarPorEstado();

    List<PromedioCategoria> promedioDiasCierrePorArea();
}
