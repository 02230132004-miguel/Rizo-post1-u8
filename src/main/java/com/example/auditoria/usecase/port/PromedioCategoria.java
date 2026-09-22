package com.example.auditoria.usecase.port;

import java.io.Serializable;

/**
 * Record DTO para representar el promedio de días de cierre por área responsable.
 */
public record PromedioCategoria(
        String categoria,
        double promedioDias
) implements Serializable {}
