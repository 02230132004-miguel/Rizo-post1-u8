package com.example.auditoria.usecase.impl;

import com.example.auditoria.usecase.ObtenerDashboardAuditoriaUseCase;
import com.example.auditoria.usecase.port.DashboardAuditoriaView;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;

import java.util.Objects;

/**
 * Servicio puro de aplicación que consolida las métricas del dashboard a través del puerto de persistencia optimizado.
 */
public class ObtenerDashboardAuditoriaService implements ObtenerDashboardAuditoriaUseCase {

    private final HallazgoRepositoryPort hallazgoRepository;

    public ObtenerDashboardAuditoriaService(HallazgoRepositoryPort hallazgoRepository) {
        this.hallazgoRepository = Objects.requireNonNull(hallazgoRepository, "El repositorio de hallazgos es requerido");
    }

    @Override
    public DashboardAuditoriaView obtenerDashboard() {
        var porSeveridad = hallazgoRepository.contarPorSeveridad();
        var porEstado = hallazgoRepository.contarPorEstado();
        var promedioDias = hallazgoRepository.promedioDiasCierrePorArea();

        return new DashboardAuditoriaView(porSeveridad, porEstado, promedioDias);
    }
}
