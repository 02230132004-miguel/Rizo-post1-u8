package com.example.auditoria.usecase.impl;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.usecase.ConsultarHallazgoUseCase;
import com.example.auditoria.usecase.port.CambioEstadoView;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Servicio puro de aplicación para consultas de hallazgos e historial de auditoría.
 */
public class ConsultarHallazgoService implements ConsultarHallazgoUseCase {

    private final HallazgoRepositoryPort hallazgoRepository;
    private final HistorialAuditoriaPort historialAuditoria;

    public ConsultarHallazgoService(
            HallazgoRepositoryPort hallazgoRepository,
            HistorialAuditoriaPort historialAuditoria
    ) {
        this.hallazgoRepository = Objects.requireNonNull(hallazgoRepository, "El repositorio de hallazgos es requerido");
        this.historialAuditoria = Objects.requireNonNull(historialAuditoria, "El puerto de historial de auditoría es requerido");
    }

    @Override
    public HallazgoAuditoria buscarPorId(HallazgoId id) {
        return hallazgoRepository.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Hallazgo no encontrado con id: " + id));
    }

    @Override
    public List<HallazgoAuditoria> buscarTodos() {
        return hallazgoRepository.buscarTodos();
    }

    @Override
    public List<CambioEstadoView> consultarHistorial(HallazgoId id) {
        // Verifica primero la existencia del hallazgo
        hallazgoRepository.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Hallazgo no encontrado con id: " + id));

        return historialAuditoria.listarPorHallazgo(id);
    }
}
