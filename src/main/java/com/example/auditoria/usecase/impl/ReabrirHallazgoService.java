package com.example.auditoria.usecase.impl;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.usecase.ReabrirHallazgoUseCase;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Servicio puro de aplicación que reabre un hallazgo cerrado y registra el motivo en la bitácora.
 */
public class ReabrirHallazgoService implements ReabrirHallazgoUseCase {

    private final HallazgoRepositoryPort hallazgoRepository;
    private final HistorialAuditoriaPort historialAuditoria;

    public ReabrirHallazgoService(
            HallazgoRepositoryPort hallazgoRepository,
            HistorialAuditoriaPort historialAuditoria
    ) {
        this.hallazgoRepository = Objects.requireNonNull(hallazgoRepository, "El repositorio de hallazgos es requerido");
        this.historialAuditoria = Objects.requireNonNull(historialAuditoria, "El puerto de historial de auditoría es requerido");
    }

    @Override
    public HallazgoAuditoria reabrir(HallazgoId id, String motivo) {
        HallazgoAuditoria hallazgo = hallazgoRepository.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Hallazgo no encontrado con id: " + id));

        EstadoHallazgo estadoAnterior = hallazgo.reabrir();
        hallazgoRepository.guardar(hallazgo);
        historialAuditoria.registrar(hallazgo.getId(), estadoAnterior, hallazgo.getEstado(), motivo);

        return hallazgo;
    }
}
