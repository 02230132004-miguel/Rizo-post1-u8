package com.example.auditoria.domain.valueobject;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Value Object inmutable que encapsula el plan de remediación acordado para el hallazgo.
 */
public record PlanRemediacion(
        String responsable,
        LocalDate fechaLimite,
        String notas
) implements Serializable {

    public PlanRemediacion {
        if (responsable == null || responsable.isBlank()) {
            throw new IllegalArgumentException("El responsable de la remediación es obligatorio");
        }
        if (fechaLimite == null) {
            throw new IllegalArgumentException("La fecha límite de remediación es obligatoria");
        }
        if (notas == null || notas.isBlank()) {
            throw new IllegalArgumentException("Las notas del plan de remediación son obligatorias");
        }
    }
}
