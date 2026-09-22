package com.example.auditoria.adapter.out.persistence;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.Severidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Entidad JPA relacional para la persistencia del hallazgo de auditoría.
 */
@Entity
@Table(name = "hallazgos")
public class HallazgoJpaEntity {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(name = "area_responsable", nullable = false)
    private String areaResponsable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severidad severidad;

    @Column(name = "fecha_deteccion", nullable = false)
    private LocalDate fechaDeteccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoHallazgo estado;

    @Column(name = "plan_responsable")
    private String planResponsable;

    @Column(name = "plan_fecha_limite")
    private LocalDate planFechaLimite;

    @Column(name = "plan_notas", length = 2000)
    private String planNotas;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    public HallazgoJpaEntity() {}

    public HallazgoJpaEntity(
            String id,
            String titulo,
            String descripcion,
            String areaResponsable,
            Severidad severidad,
            LocalDate fechaDeteccion,
            EstadoHallazgo estado,
            String planResponsable,
            LocalDate planFechaLimite,
            String planNotas,
            LocalDate fechaCierre
    ) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.areaResponsable = areaResponsable;
        this.severidad = severidad;
        this.fechaDeteccion = fechaDeteccion;
        this.estado = estado;
        this.planResponsable = planResponsable;
        this.planFechaLimite = planFechaLimite;
        this.planNotas = planNotas;
        this.fechaCierre = fechaCierre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getAreaResponsable() {
        return areaResponsable;
    }

    public void setAreaResponsable(String areaResponsable) {
        this.areaResponsable = areaResponsable;
    }

    public Severidad getSeveridad() {
        return severidad;
    }

    public void setSeveridad(Severidad severidad) {
        this.severidad = severidad;
    }

    public LocalDate getFechaDeteccion() {
        return fechaDeteccion;
    }

    public void setFechaDeteccion(LocalDate fechaDeteccion) {
        this.fechaDeteccion = fechaDeteccion;
    }

    public EstadoHallazgo getEstado() {
        return estado;
    }

    public void setEstado(EstadoHallazgo estado) {
        this.estado = estado;
    }

    public String getPlanResponsable() {
        return planResponsable;
    }

    public void setPlanResponsable(String planResponsable) {
        this.planResponsable = planResponsable;
    }

    public LocalDate getPlanFechaLimite() {
        return planFechaLimite;
    }

    public void setPlanFechaLimite(LocalDate planFechaLimite) {
        this.planFechaLimite = planFechaLimite;
    }

    public String getPlanNotas() {
        return planNotas;
    }

    public void setPlanNotas(String planNotas) {
        this.planNotas = planNotas;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }
}
