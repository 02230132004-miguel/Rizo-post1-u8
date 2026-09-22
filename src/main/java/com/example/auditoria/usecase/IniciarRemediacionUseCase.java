package com.example.auditoria.usecase;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;

/**
 * Caso de uso: Iniciar el proceso de remediación para un hallazgo existente.
 */
public interface IniciarRemediacionUseCase {

    HallazgoAuditoria iniciarRemediacion(
            HallazgoId id,
            PlanRemediacion plan,
            String motivo
    );
}
