package com.example.auditoria.adapter.out.persistence;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.usecase.port.CambioEstadoView;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adaptador de persistencia append-only que implementa HistorialAuditoriaPort.
 * Registra cada cambio de estado como un hecho inmutable para auditoría legal.
 */
@Component
public class HistorialAuditoriaAdapter implements HistorialAuditoriaPort {

    private final HistorialCambioEstadoJpaRepository jpaRepository;

    public HistorialAuditoriaAdapter(HistorialCambioEstadoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void registrar(HallazgoId hallazgoId, EstadoHallazgo anterior, EstadoHallazgo nuevo, String motivo) {
        HistorialCambioEstadoJpaEntity entidad = new HistorialCambioEstadoJpaEntity(
                hallazgoId.valor().toString(),
                anterior.name(),
                nuevo.name(),
                motivo,
                LocalDateTime.now()
        );
        jpaRepository.save(entidad);
    }

    @Override
    public List<CambioEstadoView> listarPorHallazgo(HallazgoId hallazgoId) {
        return jpaRepository.findByHallazgoIdOrderByFechaAsc(hallazgoId.valor().toString()).stream()
                .map(entity -> new CambioEstadoView(
                        entity.getEstadoAnterior(),
                        entity.getEstadoNuevo(),
                        entity.getMotivo(),
                        entity.getFecha()
                ))
                .toList();
    }
}
