package com.example.auditoria.usecase;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.usecase.port.CambioEstadoView;

import java.util.List;

/**
 * Caso de uso: Consultar hallazgos individuales, listado general e historial de cambios.
 */
public interface ConsultarHallazgoUseCase {

    HallazgoAuditoria buscarPorId(HallazgoId id);

    List<HallazgoAuditoria> buscarTodos();

    List<CambioEstadoView> consultarHistorial(HallazgoId id);
}
