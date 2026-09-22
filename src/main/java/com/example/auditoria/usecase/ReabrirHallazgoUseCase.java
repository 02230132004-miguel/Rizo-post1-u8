package com.example.auditoria.usecase;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;

/**
 * Caso de uso: Reabrir un hallazgo cerrado ante recurrencia o insuficiencia en la solución.
 */
public interface ReabrirHallazgoUseCase {

    HallazgoAuditoria reabrir(HallazgoId id, String motivo);
}
