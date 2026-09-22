package com.example.auditoria.usecase.port;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Record DTO para la consulta forense e histórica de transiciones de estado de un hallazgo.
 */
public record CambioEstadoView(
        String estadoAnterior,
        String estadoNuevo,
        String motivo,
        LocalDateTime fecha
) implements Serializable {}
