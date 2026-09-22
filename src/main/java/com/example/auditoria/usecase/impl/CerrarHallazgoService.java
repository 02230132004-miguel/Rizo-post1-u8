package com.example.auditoria.usecase.impl;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.usecase.CerrarHallazgoUseCase;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Servicio puro de aplicación que cierra un hallazgo validado y registra la bitácora de auditoría.
 */
public class CerrarHallazgoService implements CerrarHallazgoUseCase {

    private final HallazgoRepositoryPort hallazgoRepository;
    private final HistorialAuditoriaPort historialAuditoria;

    public CerrarHallazgoService(
            HallazgoRepositoryPort hallazgoRepository,
            HistorialAuditoriaPort historialAuditoria
    ) {
        this.hallazgoRepository = Objects.requireNonNull(hallazgoRepository, "El repositorio de hallazgos es requerido");
        this.historialAuditoria = Objects.requireNonNull(historialAuditoria, "El puerto de historial de auditoría es requerido");
    }

    @Override
    public HallazgoAuditoria cerrar(HallazgoId id, String motivo) {
        HallazgoAuditoria hallazgo = hallazgoRepository.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Hallazgo no encontrado con id: " + id));

        EstadoHallazgo estadoAnterior = hallazgo.cerrar();
        hallazgoRepository.guardar(hallazgo);
        historialAuditoria.registrar(hallazgo.getId(), estadoAnterior, hallazgo.getEstado(), motivo);

        return hallazgo;
    }
}
