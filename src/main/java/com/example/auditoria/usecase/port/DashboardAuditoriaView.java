package com.example.auditoria.usecase.port;

import java.io.Serializable;
import java.util.List;

/**
 * Vista consolidada de métricas agregadas para el Dashboard de Auditoría.
 */
public record DashboardAuditoriaView(
        List<ConteoCategoria> porSeveridad,
        List<ConteoCategoria> porEstado,
        List<PromedioCategoria> promedioDiasCierrePorArea
) implements Serializable {}
