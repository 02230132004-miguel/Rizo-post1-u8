package com.example.auditoria.adapter.out.persistence;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.usecase.port.ConteoCategoria;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.PromedioCategoria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia que implementa HallazgoRepositoryPort.
 * Mapea bidireccionalmente entre el modelo de dominio puro y las entidades JPA relacionales.
 */
@Component
public class HallazgoRepositoryAdapter implements HallazgoRepositoryPort {

    private final HallazgoJpaRepository jpaRepository;

    public HallazgoRepositoryAdapter(HallazgoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void guardar(HallazgoAuditoria hallazgo) {
        HallazgoJpaEntity entity = toJpaEntity(hallazgo);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<HallazgoAuditoria> buscarPorId(HallazgoId id) {
        return jpaRepository.findById(id.valor().toString())
                .map(this::toDomainEntity);
    }

    @Override
    public List<HallazgoAuditoria> buscarTodos() {
        return jpaRepository.findAll().stream()
                .map(this::toDomainEntity)
                .toList();
    }

    @Override
    public List<ConteoCategoria> contarPorSeveridad() {
        return jpaRepository.contarPorSeveridad();
    }

    @Override
    public List<ConteoCategoria> contarPorEstado() {
        return jpaRepository.contarPorEstado();
    }

    @Override
    public List<PromedioCategoria> promedioDiasCierrePorArea() {
        List<Object[]> resultados = jpaRepository.promedioDiasCierrePorAreaRaw();
        return resultados.stream()
                .map(row -> {
                    String area = (String) row[0];
                    Number promedio = (Number) row[1];
                    return new PromedioCategoria(area, promedio != null ? promedio.doubleValue() : 0.0);
                })
                .toList();
    }

    private HallazgoJpaEntity toJpaEntity(HallazgoAuditoria domain) {
        String planResponsable = null;
        java.time.LocalDate planFechaLimite = null;
        String planNotas = null;

        if (domain.getPlanRemediacion() != null) {
            planResponsable = domain.getPlanRemediacion().responsable();
            planFechaLimite = domain.getPlanRemediacion().fechaLimite();
            planNotas = domain.getPlanRemediacion().notas();
        }

        return new HallazgoJpaEntity(
                domain.getId().valor().toString(),
                domain.getTitulo(),
                domain.getDescripcion(),
                domain.getAreaResponsable(),
                domain.getSeveridad(),
                domain.getFechaDeteccion(),
                domain.getEstado(),
                planResponsable,
                planFechaLimite,
                planNotas,
                domain.getFechaCierre()
        );
    }

    private HallazgoAuditoria toDomainEntity(HallazgoJpaEntity entity) {
        PlanRemediacion plan = null;
        if (entity.getPlanResponsable() != null) {
            plan = new PlanRemediacion(
                    entity.getPlanResponsable(),
                    entity.getPlanFechaLimite(),
                    entity.getPlanNotas()
            );
        }

        return HallazgoAuditoria.reconstituir(
                HallazgoId.desde(entity.getId()),
                entity.getTitulo(),
                entity.getDescripcion(),
                entity.getAreaResponsable(),
                entity.getSeveridad(),
                entity.getFechaDeteccion(),
                entity.getEstado(),
                plan,
                entity.getFechaCierre()
        );
    }
}
