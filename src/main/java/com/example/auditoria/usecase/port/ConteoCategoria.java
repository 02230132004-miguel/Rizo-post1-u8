package com.example.auditoria.usecase.port;

import java.io.Serializable;

/**
 * Record DTO para representar el conteo agregado de hallazgos por una categoría específica (Severidad o Estado).
 */
public record ConteoCategoria(
        String categoria,
        long total
) implements Serializable {}
