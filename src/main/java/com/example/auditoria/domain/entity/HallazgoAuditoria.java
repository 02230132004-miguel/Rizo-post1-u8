package com.example.auditoria.domain.entity;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.domain.valueobject.TransicionInvalidaException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad Raíz de Agregado (Aggregate Root) del dominio de auditoría.
 * Protege sus invariantes de negocio y controla el ciclo de vida a través de transiciones de estado explícitas.
 * Cero dependencias de frameworks o infraestructura de persistencia.
 */
public class HallazgoAuditoria {

    private final HallazgoId id;
    private final String titulo;
    private final String descripcion;
    private final String areaResponsable;
    private final Severidad severidad;
    private final LocalDate fechaDeteccion;

    private EstadoHallazgo estado;
    private PlanRemediacion planRemediacion;
    private LocalDate fechaCierre;

    private HallazgoAuditoria(
            HallazgoId id,
            String titulo,
            String descripcion,
            String areaResponsable,
            Severidad severidad,
            LocalDate fechaDeteccion,
            EstadoHallazgo estado,
            PlanRemediacion planRemediacion,
            LocalDate fechaCierre
    ) {
        this.id = Objects.requireNonNull(id, "El identificador no puede ser nulo");
        this.titulo = validarTexto(titulo, "El título es obligatorio");
        this.descripcion = validarTexto(descripcion, "La descripción es obligatoria");
        this.areaResponsable = validarTexto(areaResponsable, "El área responsable es obligatoria");
        this.severidad = Objects.requireNonNull(severidad, "La severidad es obligatoria");
        this.fechaDeteccion = Objects.requireNonNull(fechaDeteccion, "La fecha de detección no puede ser nula");
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo");
        this.planRemediacion = planRemediacion;
        this.fechaCierre = fechaCierre;
    }

    /**
     * Fábrica de creación inicial para un nuevo hallazgo de auditoría.
     */
    public static HallazgoAuditoria crear(
            String titulo,
            String descripcion,
            String areaResponsable,
            Severidad severidad
    ) {
        return new HallazgoAuditoria(
                HallazgoId.nuevo(),
                titulo,
                descripcion,
                areaResponsable,
                severidad,
                LocalDate.now(),
                EstadoHallazgo.ABIERTO,
                null,
                null
        );
    }

    /**
     * Fábrica de reconstitución para reconstruir la entidad desde el repositorio de persistencia.
     */
    public static HallazgoAuditoria reconstituir(
            HallazgoId id,
            String titulo,
            String descripcion,
            String areaResponsable,
            Severidad severidad,
            LocalDate fechaDeteccion,
            EstadoHallazgo estado,
            PlanRemediacion planRemediacion,
            LocalDate fechaCierre
    ) {
        return new HallazgoAuditoria(
                id,
                titulo,
                descripcion,
                areaResponsable,
                severidad,
                fechaDeteccion,
                estado,
                planRemediacion,
                fechaCierre
        );
    }

    /**
     * Inicia el proceso de remediación asociando el plan acordado y transicionando el estado a EN_REMEDIACION.
     *
     * @param plan Plan de remediación acordado
     * @return El estado anterior antes de la transición
     */
    public EstadoHallazgo iniciarRemediacion(PlanRemediacion plan) {
        Objects.requireNonNull(plan, "El plan de remediación no puede ser nulo");
        if (!this.estado.puedeTransicionarA(EstadoHallazgo.EN_REMEDIACION)) {
            throw new TransicionInvalidaException(this.estado, EstadoHallazgo.EN_REMEDIACION);
        }
        EstadoHallazgo anterior = this.estado;
        this.estado = EstadoHallazgo.EN_REMEDIACION;
        this.planRemediacion = plan;
        return anterior;
    }

    /**
     * Cierra el hallazgo tras verificar la existencia de un plan de remediación y transicionar a CERRADO.
     * Registra la fecha de cierre actual.
     *
     * @return El estado anterior antes de la transición
     */
    public EstadoHallazgo cerrar() {
        if (this.planRemediacion == null) {
            throw new IllegalStateException("No es posible cerrar un hallazgo sin un plan de remediación previo");
        }
        if (!this.estado.puedeTransicionarA(EstadoHallazgo.CERRADO)) {
            throw new TransicionInvalidaException(this.estado, EstadoHallazgo.CERRADO);
        }
        EstadoHallazgo anterior = this.estado;
        this.estado = EstadoHallazgo.CERRADO;
        this.fechaCierre = LocalDate.now();
        return anterior;
    }

    /**
     * Reabre un hallazgo cerrado si se detecta recurrencia o insuficiencia en la solución, limpiando la fecha de cierre.
     *
     * @return El estado anterior antes de la transición
     */
    public EstadoHallazgo reabrir() {
        if (!this.estado.puedeTransicionarA(EstadoHallazgo.REABIERTO)) {
            throw new TransicionInvalidaException(this.estado, EstadoHallazgo.REABIERTO);
        }
        EstadoHallazgo anterior = this.estado;
        this.estado = EstadoHallazgo.REABIERTO;
        this.fechaCierre = null;
        return anterior;
    }

    private static String validarTexto(String texto, String mensajeError) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return texto.trim();
    }

    // Getters sin mutabilidad externa
    public HallazgoId getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getAreaResponsable() {
        return areaResponsable;
    }

    public Severidad getSeveridad() {
        return severidad;
    }

    public LocalDate getFechaDeteccion() {
        return fechaDeteccion;
    }

    public EstadoHallazgo getEstado() {
        return estado;
    }

    public PlanRemediacion getPlanRemediacion() {
        return planRemediacion;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }
}
