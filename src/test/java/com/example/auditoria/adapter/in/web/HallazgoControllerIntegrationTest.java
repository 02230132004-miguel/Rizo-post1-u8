package com.example.auditoria.adapter.in.web;

import com.example.auditoria.adapter.in.web.dto.CerrarRequest;
import com.example.auditoria.adapter.in.web.dto.HallazgoResponse;
import com.example.auditoria.adapter.in.web.dto.IniciarRemediacionRequest;
import com.example.auditoria.adapter.in.web.dto.ReabrirRequest;
import com.example.auditoria.adapter.in.web.dto.RegistrarHallazgoRequest;
import com.example.auditoria.domain.valueobject.Severidad;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class HallazgoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/hallazgos debe registrar un hallazgo exitosamente (201 Created)")
    void testRegistrarHallazgoExitoso() throws Exception {
        RegistrarHallazgoRequest request = new RegistrarHallazgoRequest(
                "Fuga de información en logs",
                "Se están imprimiendo números de tarjeta en los logs de auditoría",
                "Ciberseguridad",
                Severidad.CRITICA
        );

        mockMvc.perform(post("/api/hallazgos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.titulo", is("Fuga de información en logs")))
                .andExpect(jsonPath("$.descripcion", is("Se están imprimiendo números de tarjeta en los logs de auditoría")))
                .andExpect(jsonPath("$.areaResponsable", is("Ciberseguridad")))
                .andExpect(jsonPath("$.severidad", is("CRITICA")))
                .andExpect(jsonPath("$.estado", is("ABIERTO")))
                .andExpect(jsonPath("$.fechaDeteccion", is(LocalDate.now().toString())));
    }

    @Test
    @DisplayName("Debe ejecutar el ciclo de vida completo de transiciones permitidas con respuestas 200 OK")
    void testCicloDeVidaTransicionesPermitidas() throws Exception {
        // 1. Crear hallazgo inicial
        RegistrarHallazgoRequest createReq = new RegistrarHallazgoRequest(
                "Inyección SQL en reporte financiero",
                "Parámetro 'cuenta' no parametrizado en consulta",
                "Finanzas y TI",
                Severidad.CRITICA
        );

        MvcResult resultCrear = mockMvc.perform(post("/api/hallazgos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        HallazgoResponse hallazgo = objectMapper.readValue(
                resultCrear.getResponse().getContentAsString(),
                HallazgoResponse.class
        );
        String id = hallazgo.id();

        // 2. Iniciar Remediación (ABIERTO -> EN_REMEDIACION)
        IniciarRemediacionRequest remedReq = new IniciarRemediacionRequest(
                "Ing. Roberto Sánchez",
                LocalDate.now().plusDays(10),
                "Parametrizar consulta con PreparedStatement",
                "Asignación prioritaria por comité de seguridad"
        );

        mockMvc.perform(patch("/api/hallazgos/" + id + "/iniciar-remediacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(remedReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("EN_REMEDIACION")))
                .andExpect(jsonPath("$.responsableRemediacion", is("Ing. Roberto Sánchez")))
                .andExpect(jsonPath("$.notasRemediacion", is("Parametrizar consulta con PreparedStatement")));

        // 3. Cerrar Hallazgo (EN_REMEDIACION -> CERRADO)
        CerrarRequest cerrarReq = new CerrarRequest("Validación exitosa en entorno de pruebas");

        mockMvc.perform(patch("/api/hallazgos/" + id + "/cerrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cerrarReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CERRADO")))
                .andExpect(jsonPath("$.fechaCierre", is(LocalDate.now().toString())));

        // 4. Reabrir Hallazgo (CERRADO -> REABIERTO)
        ReabrirRequest reabrirReq = new ReabrirRequest("Se detectó variante en módulo de exportación");

        mockMvc.perform(patch("/api/hallazgos/" + id + "/reabrir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reabrirReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("REABIERTO")))
                .andExpect(jsonPath("$.fechaCierre").doesNotExist());

        // 5. Iniciar Remediación nuevamente (REABIERTO -> EN_REMEDIACION)
        IniciarRemediacionRequest remedReq2 = new IniciarRemediacionRequest(
                "Ing. Roberto Sánchez",
                LocalDate.now().plusDays(5),
                "Corrección del módulo de exportación",
                "Re-atención del caso"
        );

        mockMvc.perform(patch("/api/hallazgos/" + id + "/iniciar-remediacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(remedReq2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("EN_REMEDIACION")));
    }

    @Test
    @DisplayName("Debe rechazar con 400 Bad Request cuando se intenta cerrar un hallazgo sin plan de remediación previo")
    void testRechazoCierreSinRemediacion() throws Exception {
        RegistrarHallazgoRequest createReq = new RegistrarHallazgoRequest(
                "Puerto no seguro abierto",
                "Puerto 21 (FTP) abierto en servidor de base de datos",
                "Infraestructura",
                Severidad.ALTA
        );

        MvcResult resultCrear = mockMvc.perform(post("/api/hallazgos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        HallazgoResponse hallazgo = objectMapper.readValue(
                resultCrear.getResponse().getContentAsString(),
                HallazgoResponse.class
        );

        // Intento directo de cerrar sin remediación (estado ABIERTO)
        mockMvc.perform(patch("/api/hallazgos/" + hallazgo.id() + "/cerrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CerrarRequest("Intento directo de cierre"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensaje", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/hallazgos/dashboard debe generar métricas agregadas consolidadas")
    void testDashboardConsolidado() throws Exception {
        // Registramos varios hallazgos en distintas áreas y severidades
        RegistrarHallazgoRequest h1 = new RegistrarHallazgoRequest("H1", "Desc 1", "Finanzas", Severidad.CRITICA);
        RegistrarHallazgoRequest h2 = new RegistrarHallazgoRequest("H2", "Desc 2", "Finanzas", Severidad.ALTA);
        RegistrarHallazgoRequest h3 = new RegistrarHallazgoRequest("H3", "Desc 3", "Operaciones", Severidad.MEDIA);

        MvcResult res1 = mockMvc.perform(post("/api/hallazgos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(h1))).andReturn();

        mockMvc.perform(post("/api/hallazgos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(h2)));

        mockMvc.perform(post("/api/hallazgos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(h3)));

        String id1 = objectMapper.readValue(res1.getResponse().getContentAsString(), HallazgoResponse.class).id();

        // Cerramos el hallazgo 1 para que aporte al promedio de días de cierre
        IniciarRemediacionRequest remed = new IniciarRemediacionRequest("Resp", LocalDate.now(), "Notas", "Motivo");
        mockMvc.perform(patch("/api/hallazgos/" + id1 + "/iniciar-remediacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(remed)));

        mockMvc.perform(patch("/api/hallazgos/" + id1 + "/cerrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CerrarRequest("Listo"))));

        // Consultamos el dashboard
        mockMvc.perform(get("/api/hallazgos/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porSeveridad", notNullValue()))
                .andExpect(jsonPath("$.porEstado", notNullValue()))
                .andExpect(jsonPath("$.promedioDiasCierrePorArea", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/hallazgos/{id}/historial debe reflejar la secuencia append-only de transiciones")
    void testHistorialAppendOnly() throws Exception {
        RegistrarHallazgoRequest createReq = new RegistrarHallazgoRequest(
                "Certificado SSL expirado",
                "Certificado del portal caducó hace 2 días",
                "Infraestructura",
                Severidad.ALTA
        );

        MvcResult resultCrear = mockMvc.perform(post("/api/hallazgos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readValue(resultCrear.getResponse().getContentAsString(), HallazgoResponse.class).id();

        // Transición 1: Iniciar remediación
        IniciarRemediacionRequest remedReq = new IniciarRemediacionRequest(
                "Ing. Pedro",
                LocalDate.now().plusDays(2),
                "Renovación con Let's Encrypt",
                "Motivo: Alerta de monitoreo"
        );
        mockMvc.perform(patch("/api/hallazgos/" + id + "/iniciar-remediacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(remedReq)));

        // Transición 2: Cerrar
        mockMvc.perform(patch("/api/hallazgos/" + id + "/cerrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CerrarRequest("Certificado desplegado exitosamente"))));

        // Transición 3: Reabrir
        mockMvc.perform(patch("/api/hallazgos/" + id + "/reabrir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReabrirRequest("Inconsistencia en cadena intermedia"))));

        // Consultar Historial
        mockMvc.perform(get("/api/hallazgos/" + id + "/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].estadoAnterior", is("ABIERTO")))
                .andExpect(jsonPath("$[0].estadoNuevo", is("EN_REMEDIACION")))
                .andExpect(jsonPath("$[0].motivo", is("Motivo: Alerta de monitoreo")))
                .andExpect(jsonPath("$[1].estadoAnterior", is("EN_REMEDIACION")))
                .andExpect(jsonPath("$[1].estadoNuevo", is("CERRADO")))
                .andExpect(jsonPath("$[1].motivo", is("Certificado desplegado exitosamente")))
                .andExpect(jsonPath("$[2].estadoAnterior", is("CERRADO")))
                .andExpect(jsonPath("$[2].estadoNuevo", is("REABIERTO")))
                .andExpect(jsonPath("$[2].motivo", is("Inconsistencia en cadena intermedia")));
    }

    @Test
    @DisplayName("GET /api/hallazgos/{id} con ID inexistente debe retornar 404 Not Found")
    void testBuscarPorIdInexistente() throws Exception {
        String idInexistente = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/hallazgos/" + idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Recurso No Encontrado")));
    }
}
