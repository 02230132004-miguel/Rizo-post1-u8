package com.example.auditoria.adapter.in.web;

import com.example.auditoria.adapter.in.web.dto.CerrarRequest;
import com.example.auditoria.adapter.in.web.dto.HallazgoResponse;
import com.example.auditoria.adapter.in.web.dto.IniciarRemediacionRequest;
import com.example.auditoria.adapter.in.web.dto.ReabrirRequest;
import com.example.auditoria.adapter.in.web.dto.RegistrarHallazgoRequest;
import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.usecase.CerrarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHallazgoUseCase;
import com.example.auditoria.usecase.IniciarRemediacionUseCase;
import com.example.auditoria.usecase.ObtenerDashboardAuditoriaUseCase;
import com.example.auditoria.usecase.ReabrirHallazgoUseCase;
import com.example.auditoria.usecase.RegistrarHallazgoUseCase;
import com.example.auditoria.usecase.port.CambioEstadoView;
import com.example.auditoria.usecase.port.DashboardAuditoriaView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada Web (REST Controller) que expone los casos de uso del dominio de auditoría.
 */
@RestController
@RequestMapping("/api/hallazgos")
public class HallazgoController {

    private final RegistrarHallazgoUseCase registrarHallazgoUseCase;
    private final IniciarRemediacionUseCase iniciarRemediacionUseCase;
    private final CerrarHallazgoUseCase cerrarHallazgoUseCase;
    private final ReabrirHallazgoUseCase reabrirHallazgoUseCase;
    private final ConsultarHallazgoUseCase consultarHallazgoUseCase;
    private final ObtenerDashboardAuditoriaUseCase obtenerDashboardAuditoriaUseCase;

    public HallazgoController(
            RegistrarHallazgoUseCase registrarHallazgoUseCase,
            IniciarRemediacionUseCase iniciarRemediacionUseCase,
            CerrarHallazgoUseCase cerrarHallazgoUseCase,
            ReabrirHallazgoUseCase reabrirHallazgoUseCase,
            ConsultarHallazgoUseCase consultarHallazgoUseCase,
            ObtenerDashboardAuditoriaUseCase obtenerDashboardAuditoriaUseCase
    ) {
        this.registrarHallazgoUseCase = registrarHallazgoUseCase;
        this.iniciarRemediacionUseCase = iniciarRemediacionUseCase;
        this.cerrarHallazgoUseCase = cerrarHallazgoUseCase;
        this.reabrirHallazgoUseCase = reabrirHallazgoUseCase;
        this.consultarHallazgoUseCase = consultarHallazgoUseCase;
        this.obtenerDashboardAuditoriaUseCase = obtenerDashboardAuditoriaUseCase;
    }

    @PostMapping
    public ResponseEntity<HallazgoResponse> registrar(@Valid @RequestBody RegistrarHallazgoRequest request) {
        HallazgoAuditoria hallazgo = registrarHallazgoUseCase.registrar(
                request.titulo(),
                request.descripcion(),
                request.areaResponsable(),
                request.severidad()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(HallazgoResponse.fromDomain(hallazgo));
    }

    @PatchMapping("/{id}/iniciar-remediacion")
    public ResponseEntity<HallazgoResponse> iniciarRemediacion(
            @PathVariable String id,
            @Valid @RequestBody IniciarRemediacionRequest request
    ) {
        PlanRemediacion plan = new PlanRemediacion(
                request.responsable(),
                request.fechaLimite(),
                request.notas()
        );
        HallazgoAuditoria hallazgo = iniciarRemediacionUseCase.iniciarRemediacion(
                HallazgoId.desde(id),
                plan,
                request.motivo() != null ? request.motivo() : "Inicio formal de remediación"
        );
        return ResponseEntity.ok(HallazgoResponse.fromDomain(hallazgo));
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<HallazgoResponse> cerrar(
            @PathVariable String id,
            @RequestBody(required = false) CerrarRequest request
    ) {
        String motivo = (request != null && request.motivo() != null) ? request.motivo() : "Cierre verificado de hallazgo";
        HallazgoAuditoria hallazgo = cerrarHallazgoUseCase.cerrar(HallazgoId.desde(id), motivo);
        return ResponseEntity.ok(HallazgoResponse.fromDomain(hallazgo));
    }

    @PatchMapping("/{id}/reabrir")
    public ResponseEntity<HallazgoResponse> reabrir(
            @PathVariable String id,
            @RequestBody(required = false) ReabrirRequest request
    ) {
        String motivo = (request != null && request.motivo() != null) ? request.motivo() : "Reapertura de hallazgo por recurrencia";
        HallazgoAuditoria hallazgo = reabrirHallazgoUseCase.reabrir(HallazgoId.desde(id), motivo);
        return ResponseEntity.ok(HallazgoResponse.fromDomain(hallazgo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallazgoResponse> buscarPorId(@PathVariable String id) {
        HallazgoAuditoria hallazgo = consultarHallazgoUseCase.buscarPorId(HallazgoId.desde(id));
        return ResponseEntity.ok(HallazgoResponse.fromDomain(hallazgo));
    }

    @GetMapping
    public ResponseEntity<List<HallazgoResponse>> buscarTodos() {
        List<HallazgoResponse> response = consultarHallazgoUseCase.buscarTodos().stream()
                .map(HallazgoResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardAuditoriaView> obtenerDashboard() {
        DashboardAuditoriaView dashboard = obtenerDashboardAuditoriaUseCase.obtenerDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<CambioEstadoView>> consultarHistorial(@PathVariable String id) {
        List<CambioEstadoView> historial = consultarHallazgoUseCase.consultarHistorial(HallazgoId.desde(id));
        return ResponseEntity.ok(historial);
    }
}
