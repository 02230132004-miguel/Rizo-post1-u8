package com.example.auditoria.adapter.in.web.dto;

import com.example.auditoria.domain.entity.HallazgoAuditoria;

import java.time.LocalDate;

public record HallazgoResponse(
        String id,
        String titulo,
        String descripcion,
        String areaResponsable,
        String severidad,
        LocalDate fechaDeteccion,
        String estado,
        String responsableRemediacion,
        LocalDate fechaLimiteRemediacion,
        String notasRemediacion,
        LocalDate fechaCierre
) {
    public static HallazgoResponse fromDomain(HallazgoAuditoria h) {
        String responsable = null;
        LocalDate fechaLimite = null;
        String notas = null;

        if (h.getPlanRemediacion() != null) {
            responsable = h.getPlanRemediacion().responsable();
            fechaLimite = h.getPlanRemediacion().fechaLimite();
            notas = h.getPlanRemediacion().notas();
        }

        return new HallazgoResponse(
                h.getId().valor().toString(),
                h.getTitulo(),
                h.getDescripcion(),
                h.getAreaResponsable(),
                h.getSeveridad().name(),
                h.getFechaDeteccion(),
                h.getEstado().name(),
                responsable,
                fechaLimite,
                notas,
                h.getFechaCierre()
        );
    }
}
