package com.example.auditoria.usecase.port;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;

import java.util.List;

/**
 * Puerto de salida para el registro append-only de auditoría legal de cambios de estado.
 */
public interface HistorialAuditoriaPort {

    void registrar(HallazgoId hallazgoId, EstadoHallazgo anterior, EstadoHallazgo nuevo, String motivo);

    List<CambioEstadoView> listarPorHallazgo(HallazgoId hallazgoId);
}
