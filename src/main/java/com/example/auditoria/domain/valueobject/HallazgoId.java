package com.example.auditoria.domain.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Identificador inmutable del Hallazgo de Auditoría.
 * Cero dependencias de frameworks externos.
 */
public record HallazgoId(UUID valor) implements Serializable {

    public HallazgoId {
        Objects.requireNonNull(valor, "El valor del identificador no puede ser nulo");
    }

    public static HallazgoId nuevo() {
        return new HallazgoId(UUID.randomUUID());
    }

    public static HallazgoId desde(String id) {
        Objects.requireNonNull(id, "El identificador en texto no puede ser nulo");
        return new HallazgoId(UUID.fromString(id));
    }

    public static HallazgoId desde(UUID id) {
        return new HallazgoId(id);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
