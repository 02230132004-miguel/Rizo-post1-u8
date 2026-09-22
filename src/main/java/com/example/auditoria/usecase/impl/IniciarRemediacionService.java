package com.example.auditoria.usecase.impl;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.usecase.IniciarRemediacionUseCase;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Servicio puro de aplicación que inicia la remediación y asocia el plan,
 * registrando de forma append-only la transición para trazabilidad legal.
 */
public class IniciarRemediacionService implements IniciarRemediacionUseCase {

    private final HallazgoRepositoryPort hallazgoRepository;
    private final HistorialAuditoriaPort historialAuditoria;

    public IniciarRemediacionService(
            HallazgoRepositoryPort hallazgoRepository,
            HistorialAuditoriaPort historialAuditoria
    ) {
        this.hallazgoRepository = Objects.requireNonNull(hallazgoRepository, "El repositorio de hallazgos es requerido");
        this.historialAuditoria = Objects.requireNonNull(historialAuditoria, "El puerto de historial de auditoría es requerido");
    }

    @Override
    public HallazgoAuditoria iniciarRemediacion(
            HallazgoId id,
            PlanRemediacion plan,
            String motivo
    ) {
        HallazgoAuditoria hallazgo = hallazgoRepository.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Hallazgo no encontrado con id: " + id));

        EstadoHallazgo estadoAnterior = hallazgo.iniciarRemediacion(plan);
        hallazgoRepository.guardar(hallazgo);
        historialAuditoria.registrar(hallazgo.getId(), estadoAnterior, hallazgo.getEstado(), motivo);

        return hallazgo;
    }
}
