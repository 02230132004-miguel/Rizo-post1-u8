package com.example.auditoria.usecase.impl;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.usecase.RegistrarHallazgoUseCase;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;

import java.util.Objects;

/**
 * Servicio puro de aplicación que orquesta el registro de un nuevo hallazgo.
 * Cero dependencias de anotaciones de Spring.
 */
public class RegistrarHallazgoService implements RegistrarHallazgoUseCase {

    private final HallazgoRepositoryPort hallazgoRepository;

    public RegistrarHallazgoService(HallazgoRepositoryPort hallazgoRepository) {
        this.hallazgoRepository = Objects.requireNonNull(hallazgoRepository, "El repositorio de hallazgos es requerido");
    }

    @Override
    public HallazgoAuditoria registrar(
            String titulo,
            String descripcion,
            String areaResponsable,
            Severidad severidad
    ) {
        HallazgoAuditoria nuevoHallazgo = HallazgoAuditoria.crear(titulo, descripcion, areaResponsable, severidad);
        hallazgoRepository.guardar(nuevoHallazgo);
        return nuevoHallazgo;
    }
}
