package com.example.auditoria.usecase;

import com.example.auditoria.usecase.port.DashboardAuditoriaView;

/**
 * Caso de uso: Obtener métricas consolidadas del dashboard de auditoría.
 */
public interface ObtenerDashboardAuditoriaUseCase {

    DashboardAuditoriaView obtenerDashboard();
}
