package com.example.auditoria.domain.valueobject;

/**
 * Máquina de estados finita que modela el ciclo de vida de un hallazgo de auditoría.
 */
public enum EstadoHallazgo {
    ABIERTO,
    EN_REMEDIACION,
    CERRADO,
    REABIERTO;

    /**
     * Evalúa si es válida la transición hacia el estado de destino.
     * Reglas de transición:
     * - ABIERTO -> EN_REMEDIACION
     * - EN_REMEDIACION -> CERRADO
     * - CERRADO -> REABIERTO
     * - REABIERTO -> EN_REMEDIACION
     *
     * @param destino Estado objetivo
     * @return true si la transición está permitida por el modelo de negocio, false en caso contrario
     */
    public boolean puedeTransicionarA(EstadoHallazgo destino) {
        if (destino == null) {
            return false;
        }
        return switch (this) {
            case ABIERTO -> destino == EN_REMEDIACION;
            case EN_REMEDIACION -> destino == CERRADO;
            case CERRADO -> destino == REABIERTO;
            case REABIERTO -> destino == EN_REMEDIACION;
        };
    }
}
