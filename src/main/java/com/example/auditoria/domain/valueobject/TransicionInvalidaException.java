package com.example.auditoria.domain.valueobject;

/**
 * Excepción de dominio lanzada cuando se intenta una transición de estado ilegal en el ciclo de vida del hallazgo.
 */
public class TransicionInvalidaException extends RuntimeException {

    private final EstadoHallazgo origen;
    private final EstadoHallazgo destino;

    public TransicionInvalidaException(EstadoHallazgo origen, EstadoHallazgo destino) {
        super(String.format("Transición de estado inválida: no es posible pasar de %s a %s", origen, destino));
        this.origen = origen;
        this.destino = destino;
    }

    public EstadoHallazgo getOrigen() {
        return origen;
    }

    public EstadoHallazgo getDestino() {
        return destino;
    }
}
