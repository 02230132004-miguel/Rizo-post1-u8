package com.example.auditoria.domain;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.domain.valueobject.TransicionInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias puras para las entidades y value objects del dominio.
 * Ejecución 100% aislada de Spring y de la capa de persistencia.
 */
class HallazgoAuditoriaDomainTest {

    @Test
    @DisplayName("Debe crear un hallazgo en estado inicial ABIERTO con ID generado")
    void debeCrearHallazgoEnEstadoAbierto() {
        HallazgoAuditoria hallazgo = HallazgoAuditoria.crear(
                "Falta de cifrado en reposo",
                "Las contraseñas de la base de datos están en texto plano",
                "Seguridad de la Información",
                Severidad.CRITICA
        );

        assertNotNull(hallazgo.getId());
        assertNotNull(hallazgo.getId().valor());
        assertEquals("Falta de cifrado en reposo", hallazgo.getTitulo());
        assertEquals("Las contraseñas de la base de datos están en texto plano", hallazgo.getDescripcion());
        assertEquals("Seguridad de la Información", hallazgo.getAreaResponsable());
        assertEquals(Severidad.CRITICA, hallazgo.getSeveridad());
        assertEquals(EstadoHallazgo.ABIERTO, hallazgo.getEstado());
        assertEquals(LocalDate.now(), hallazgo.getFechaDeteccion());
        assertNull(hallazgo.getPlanRemediacion());
        assertNull(hallazgo.getFechaCierre());
    }

    @Test
    @DisplayName("Debe ejecutar el ciclo de vida completo de transiciones válidas")
    void debeEjecutarCicloDeVidaValido() {
        HallazgoAuditoria hallazgo = HallazgoAuditoria.crear(
                "Vulnerabilidad XSS",
                "Formulario de comentarios vulnerable",
                "Desarrollo Web",
                Severidad.ALTA
        );

        // 1. ABIERTO -> EN_REMEDIACION
        PlanRemediacion plan1 = new PlanRemediacion(
                "Ing. Juan Pérez",
                LocalDate.now().plusDays(15),
                "Sanitización con OWASP Java Encoder"
        );
        EstadoHallazgo estadoPrevio1 = hallazgo.iniciarRemediacion(plan1);
        assertEquals(EstadoHallazgo.ABIERTO, estadoPrevio1);
        assertEquals(EstadoHallazgo.EN_REMEDIACION, hallazgo.getEstado());
        assertEquals(plan1, hallazgo.getPlanRemediacion());
        assertNull(hallazgo.getFechaCierre());

        // 2. EN_REMEDIACION -> CERRADO
        EstadoHallazgo estadoPrevio2 = hallazgo.cerrar();
        assertEquals(EstadoHallazgo.EN_REMEDIACION, estadoPrevio2);
        assertEquals(EstadoHallazgo.CERRADO, hallazgo.getEstado());
        assertEquals(LocalDate.now(), hallazgo.getFechaCierre());

        // 3. CERRADO -> REABIERTO
        EstadoHallazgo estadoPrevio3 = hallazgo.reabrir();
        assertEquals(EstadoHallazgo.CERRADO, estadoPrevio3);
        assertEquals(EstadoHallazgo.REABIERTO, hallazgo.getEstado());
        assertNull(hallazgo.getFechaCierre());

        // 4. REABIERTO -> EN_REMEDIACION
        PlanRemediacion plan2 = new PlanRemediacion(
                "Ing. María Gomez",
                LocalDate.now().plusDays(7),
                "Refuerzo con CSP Headers"
        );
        EstadoHallazgo estadoPrevio4 = hallazgo.iniciarRemediacion(plan2);
        assertEquals(EstadoHallazgo.REABIERTO, estadoPrevio4);
        assertEquals(EstadoHallazgo.EN_REMEDIACION, hallazgo.getEstado());
        assertEquals(plan2, hallazgo.getPlanRemediacion());
    }

    @Test
    @DisplayName("No debe permitir cerrar directamente un hallazgo en estado ABIERTO (sin remediación)")
    void noDebePermitirCerrarHallazgoAbierto() {
        HallazgoAuditoria hallazgo = HallazgoAuditoria.crear(
                "Configuración débil de TLS",
                "Soporte para TLS 1.0 activo",
                "Infraestructura",
                Severidad.MEDIA
        );

        // Intento de cerrar hallazgo abierto (falla por falta de plan y por transición ilegal)
        assertThrows(IllegalStateException.class, hallazgo::cerrar);
    }

    @Test
    @DisplayName("No debe permitir transiciones ilegales lanzando TransicionInvalidaException")
    void noDebePermitirTransicionesIlegales() {
        HallazgoAuditoria hallazgo = HallazgoAuditoria.crear(
                "Permisos excesivos",
                "Usuarios con rol admin por defecto",
                "Sistemas",
                Severidad.ALTA
        );

        // ABIERTO no puede transicionar a REABIERTO
        TransicionInvalidaException exReabrir = assertThrows(
                TransicionInvalidaException.class,
                hallazgo::reabrir
        );
        assertEquals(EstadoHallazgo.ABIERTO, exReabrir.getOrigen());
        assertEquals(EstadoHallazgo.REABIERTO, exReabrir.getDestino());

        // Iniciamos remediación
        PlanRemediacion plan = new PlanRemediacion("Admin", LocalDate.now().plusDays(5), "Ajuste de roles");
        hallazgo.iniciarRemediacion(plan);

        // EN_REMEDIACION no puede transicionar a REABIERTO
        TransicionInvalidaException exRemediacionReabrir = assertThrows(
                TransicionInvalidaException.class,
                hallazgo::reabrir
        );
        assertEquals(EstadoHallazgo.EN_REMEDIACION, exRemediacionReabrir.getOrigen());
        assertEquals(EstadoHallazgo.REABIERTO, exRemediacionReabrir.getDestino());

        // Cerramos el hallazgo
        hallazgo.cerrar();

        // CERRADO no puede transicionar directamente a EN_REMEDIACION
        TransicionInvalidaException exCerradoRemediar = assertThrows(
                TransicionInvalidaException.class,
                () -> hallazgo.iniciarRemediacion(plan)
        );
        assertEquals(EstadoHallazgo.CERRADO, exCerradoRemediar.getOrigen());
        assertEquals(EstadoHallazgo.EN_REMEDIACION, exCerradoRemediar.getDestino());
    }

    @ParameterizedTest(name = "Transición desde {0} hacia {1} debe ser {2}")
    @CsvSource({
            "ABIERTO, EN_REMEDIACION, true",
            "ABIERTO, CERRADO, false",
            "ABIERTO, REABIERTO, false",
            "ABIERTO, ABIERTO, false",
            "EN_REMEDIACION, CERRADO, true",
            "EN_REMEDIACION, ABIERTO, false",
            "EN_REMEDIACION, REABIERTO, false",
            "EN_REMEDIACION, EN_REMEDIACION, false",
            "CERRADO, REABIERTO, true",
            "CERRADO, ABIERTO, false",
            "CERRADO, EN_REMEDIACION, false",
            "CERRADO, CERRADO, false",
            "REABIERTO, EN_REMEDIACION, true",
            "REABIERTO, CERRADO, false",
            "REABIERTO, ABIERTO, false",
            "REABIERTO, REABIERTO, false"
    })
    @DisplayName("Validación exhaustiva de la máquina de estados finita EstadoHallazgo")
    void testMatrizDeTransicionesEstadoHallazgo(EstadoHallazgo origen, EstadoHallazgo destino, boolean esperado) {
        assertEquals(esperado, origen.puedeTransicionarA(destino));
    }

    @Test
    @DisplayName("PlanRemediacion debe validar obligatoriedad de campos en su constructor compacto")
    void testValidacionPlanRemediacion() {
        assertThrows(IllegalArgumentException.class, () -> new PlanRemediacion(null, LocalDate.now(), "Notas"));
        assertThrows(IllegalArgumentException.class, () -> new PlanRemediacion("   ", LocalDate.now(), "Notas"));
        assertThrows(IllegalArgumentException.class, () -> new PlanRemediacion("Responsable", null, "Notas"));
        assertThrows(IllegalArgumentException.class, () -> new PlanRemediacion("Responsable", LocalDate.now(), null));
        assertThrows(IllegalArgumentException.class, () -> new PlanRemediacion("Responsable", LocalDate.now(), "   "));

        assertDoesNotThrow(() -> new PlanRemediacion("Responsable", LocalDate.now(), "Notas válidas"));
    }

    @Test
    @DisplayName("HallazgoId debe validar que el UUID no sea nulo")
    void testValidacionHallazgoId() {
        assertThrows(NullPointerException.class, () -> new HallazgoId(null));
        assertThrows(NullPointerException.class, () -> HallazgoId.desde((String) null));

        UUID uuid = UUID.randomUUID();
        HallazgoId id = HallazgoId.desde(uuid.toString());
        assertEquals(uuid, id.valor());
        assertEquals(uuid.toString(), id.toString());
    }
}
