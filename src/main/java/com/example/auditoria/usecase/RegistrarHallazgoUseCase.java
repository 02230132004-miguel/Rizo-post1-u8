package com.example.auditoria.usecase;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.Severidad;

/**
 * Caso de uso: Registrar un nuevo hallazgo de auditoría.
 */
public interface RegistrarHallazgoUseCase {

    HallazgoAuditoria registrar(
            String titulo,
            String descripcion,
            String areaResponsable,
            Severidad severidad
    );
}
