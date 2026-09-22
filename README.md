# Post-contenido — Unidad 8: Patrones Arquitectónicos II: Clean Architecture y Análisis Costo-Beneficio de CQRS/Event Sourcing

**Autor:** Miguel Angel Rizo Arias  
**Asignatura / Módulo:** Arquitectura de Software y Patrones de Diseño  
**Stack Tecnológico:** Java 17, Spring Boot 3.2.3, Spring Data JPA, H2 Database (In-Memory), JUnit 5, Maven  

---

## 1. Resumen Ejecutivo del Proyecto

El presente proyecto implementa la solución integral de la **Unidad 8**, modelando un sistema de **Gestión y Auditoría de Hallazgos de Seguridad / Calidad** mediante los principios formales de **Clean Architecture** (Arquitectura Limpia de Robert C. Martin). 

El sistema garantiza:
1. **Aislamiento Total del Dominio:** Las entidades (`domain.entity`) y los Value Objects (`domain.valueobject`) carecen por completo de dependencias hacia frameworks externos (Spring, Jakarta Persistence, Hibernate).
2. **Casos de Uso Desacoplados:** Los casos de uso y puertos (`usecase`) encapsulan la lógica de aplicación sin anotaciones de Spring (`@Service`, `@Autowired`), permitiendo que el núcleo sea 100% testeable de forma unitaria.
3. **Análisis Costo-Beneficio de CQRS y Event Sourcing:** En lugar de incurrir en sobre-ingeniería con arquitecturas asíncronas complejas (Kafka, EventStoreDB, Elasticsearch), se implementa una **extensión liviana y pragmática** que resuelve consultas analíticas agregadas (Dashboard) y una **bitácora legal append-only** sobre el mismo motor relacional H2.

---

## 2. Diagrama Arquitectónico de Círculos Concéntricos

De acuerdo con el principio de dependencia de Clean Architecture, **las dependencias del código fuente siempre apuntan hacia adentro**, hacia las políticas de más alto nivel:

```
+---------------------------------------------------------------------------------------+
|  4. FRAMEWORKS & DRIVERS (Externo)                                                    |
|     - Spring Boot 3.2 (AuditoriaHallazgosApplication)                                 |
|     - Spring IoC Container Configuration (AuditoriaConfiguration)                     |
|     - In-Memory H2 Database Engine & /h2-console                                      |
|                                                                                       |
|   +-------------------------------------------------------------------------------+   |
|   |  3. INTERFACE ADAPTERS                                                        |   |
|   |     - Web Inbound: HallazgoController (@RestController), RestExceptionHandler  |   |
|   |     - Web DTOs: RegistrarHallazgoRequest, IniciarRemediacionRequest, Responses|   |
|   |     - Persistence Outbound: HallazgoRepositoryAdapter, HistorialAuditoriaAdapter|  |
|   |     - Spring Data JPA: HallazgoJpaRepository, HistorialCambioEstadoJpaRepository|  |
|   |     - Entities JPA: HallazgoJpaEntity, HistorialCambioEstadoJpaEntity         |   |
|   |                                                                               |   |
|   |   +-----------------------------------------------------------------------+   |   |
|   |   |  2. USE CASES (Lógica de Aplicación Pura - Sin Spring)               |   |   |
|   |   |     - Ports: HallazgoRepositoryPort, HistorialAuditoriaPort           |   |   |
|   |   |     - DTOs / Records: DashboardAuditoriaView, CambioEstadoView        |   |   |
|   |   |     - Interfaces: RegistrarHallazgoUseCase, IniciarRemediacionUseCase |   |   |
|   |   |     - Services: RegistrarHallazgoService, IniciarRemediacionService   |   |   |
|   |   |     - Services: CerrarHallazgoService, ReabrirHallazgoService         |   |   |
|   |   |     - Services: ConsultarHallazgoService, ObtenerDashboardService     |   |   |
|   |   |                                                                       |   |   |
|   |   |   +---------------------------------------------------------------+   |   |   |
|   |   |   |  1. ENTITIES (Dominio Puro - Cero Dependencias)               |   |   |   |
|   |   |   |     - Aggregate Root: HallazgoAuditoria                       |   |   |   |
|   |   |   |     - Finite State Machine: EstadoHallazgo                    |   |   |   |
|   |   |   |     - Value Objects: HallazgoId, Severidad, PlanRemediacion   |   |   |   |
|   |   |   |     - Domain Exception: TransicionInvalidaException           |   |   |   |
|   |   |   +---------------------------------------------------------------+   |   |   |
|   |   +-----------------------------------------------------------------------+   |   |
|   +-------------------------------------------------------------------------------+   |
+---------------------------------------------------------------------------------------+
```

---

## 3. Estructura y Árbol de Paquetes del Repositorio

```
c:\Users\MIGUEL RIZO\Rizo-post1-u8\
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── resources/
    │   │   └── application.properties           # Configuración H2, JPA y Server 8080
    │   └── java/com/example/auditoria/
    │       ├── AuditoriaHallazgosApplication.java # Clase principal Spring Boot
    │       │
    │       ├── domain/                          # [CÍRCULO 1: DOMAIN ENTITIES]
    │       │   ├── entity/
    │       │   │   └── HallazgoAuditoria.java   # Aggregate Root inmutable/controlado
    │       │   └── valueobject/
    │       │       ├── HallazgoId.java          # Record con UUID no nulo
    │       │       ├── Severidad.java           # Enum (CRITICA, ALTA, MEDIA, BAJA)
    │       │       ├── EstadoHallazgo.java      # Enum con máquina de estados finita
    │       │       ├── PlanRemediacion.java     # Record validado en constructor compacto
    │       │       └── TransicionInvalidaException.java # Excepción pura de dominio
    │       │
    │       ├── usecase/                         # [CÍRCULO 2: USE CASES]
    │       │   ├── RegistrarHallazgoUseCase.java
    │       │   ├── IniciarRemediacionUseCase.java
    │       │   ├── CerrarHallazgoUseCase.java
    │       │   ├── ReabrirHallazgoUseCase.java
    │       │   ├── ConsultarHallazgoUseCase.java
    │       │   ├── ObtenerDashboardAuditoriaUseCase.java
    │       │   │
    │       │   ├── port/
    │       │   │   ├── HallazgoRepositoryPort.java # Puerto CRUD + agregaciones Dashboard
    │       │   │   ├── HistorialAuditoriaPort.java # Puerto bitácora append-only
    │       │   │   ├── ConteoCategoria.java        # Record proyección conteo
    │       │   │   ├── PromedioCategoria.java      # Record proyección promedios
    │       │   │   ├── DashboardAuditoriaView.java # Record consolidado de métricas
    │       │   │   └── CambioEstadoView.java       # Record DTO para historial forense
    │       │   │
    │       │   └── impl/                        # Clases Java puras sin anotaciones @Service
    │       │       ├── RegistrarHallazgoService.java
    │       │       ├── IniciarRemediacionService.java
    │       │       ├── CerrarHallazgoService.java
    │       │       ├── ReabrirHallazgoService.java
    │       │       ├── ConsultarHallazgoService.java
    │       │       └── ObtenerDashboardAuditoriaService.java
    │       │
    │       ├── adapter/                         # [CÍRCULO 3: INTERFACE ADAPTERS]
    │       │   ├── in/web/
    │       │   │   ├── HallazgoController.java  # @RestController endpoints /api/hallazgos
    │       │   │   ├── RestExceptionHandler.java# @RestControllerAdvice (400, 404)
    │       │   │   └── dto/
    │       │   │       ├── RegistrarHallazgoRequest.java
    │       │   │       ├── IniciarRemediacionRequest.java
    │       │   │       ├── CerrarRequest.java
    │       │   │       ├── ReabrirRequest.java
    │       │   │       ├── HallazgoResponse.java
    │       │   │       └── ErrorResponse.java
    │       │   │
    │       │   └── out/persistence/
    │       │       ├── HallazgoJpaEntity.java               # @Entity tabla "hallazgos"
    │       │       ├── HistorialCambioEstadoJpaEntity.java  # @Entity tabla "historial_cambios_estado"
    │       │       ├── HallazgoJpaRepository.java           # JpaRepository con queries JPQL/nativas
    │       │       ├── HistorialCambioEstadoJpaRepository.java # JpaRepository para bitácora
    │       │       ├── HallazgoRepositoryAdapter.java       # @Component impl HallazgoRepositoryPort
    │       │       └── HistorialAuditoriaAdapter.java       # @Component impl HistorialAuditoriaPort
    │       │
    │       └── config/                          # [CÍRCULO 4: FRAMEWORKS & DRIVERS]
    │           └── AuditoriaConfiguration.java  # Fábrica de @Beans para los casos de uso
    │
    └── test/java/com/example/auditoria/
        ├── domain/
        │   └── HallazgoAuditoriaDomainTest.java # JUnit 5 puro sin contexto Spring
        └── adapter/in/web/
            └── HallazgoControllerIntegrationTest.java # MockMvc + SpringBootTest + H2
```

---

## 4. Instrucciones de Compilación, Ejecución y Pruebas

### Requisitos Previos
- Java Development Kit (JDK) 17 o superior.
- Apache Maven 3.8+.

### Comandos de Ejecución

1. **Ejecutar Pruebas Automatizadas (Unitarias y de Integración):**
   ```bash
   mvn test
   ```

2. **Compilar y Empaquetar el Proyecto:**
   ```bash
   mvn clean package
   ```

3. **Iniciar la Aplicación Spring Boot:**
   ```bash
   mvn spring-boot:run
   ```

4. **Acceder a la Consola de Base de Datos H2:**
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:auditoria_db`
   - Usuario: `sa`
   - Contraseña: *(vacía)*

---

## 5. Endpoints REST Expuestos

| Método | Endpoint | Descripción | Código HTTP Exitoso |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/hallazgos` | Registra un nuevo hallazgo en estado `ABIERTO` | `201 Created` |
| `PATCH` | `/api/hallazgos/{id}/iniciar-remediacion` | Asocia plan y transiciona a `EN_REMEDIACION` | `200 OK` |
| `PATCH` | `/api/hallazgos/{id}/cerrar` | Valida plan, fecha de cierre y transiciona a `CERRADO` | `200 OK` |
| `PATCH` | `/api/hallazgos/{id}/reabrir` | Reabre hallazgo por recurrencia a `REABIERTO` | `200 OK` |
| `GET` | `/api/hallazgos/{id}` | Consulta el detalle de un hallazgo por ID | `200 OK` |
| `GET` | `/api/hallazgos` | Lista todos los hallazgos registrados | `200 OK` |
| `GET` | `/api/hallazgos/dashboard` | Retorna métricas analíticas agrupadas (CQRS Liviano) | `200 OK` |
| `GET` | `/api/hallazgos/{id}/historial` | Retorna bitácora legal forense *append-only* | `200 OK` |

---

## 6. Respuestas Detalladas a la Tabla de Análisis del Paso 8: CQRS y Event Sourcing

A continuación se presenta el análisis arquitectónico formal sobre la conveniencia, costos y beneficios de adoptar CQRS y Event Sourcing en sus variantes completas vs. livianas:

| Criterio de Evaluación | Análisis y Justificación Arquitectónica | Decisión Adoptada |
| :--- | :--- | :--- |
| **1. Escala y Carga** | El sistema de auditoría interna opera a nivel departamental/institucional con un volumen estimado de cientos o miles de hallazgos por año y baja concurrencia (comités de auditoría y oficiales de cumplimiento). Separar físicamente la infraestructura en microservicios independientes de lectura y escritura o introducir clusters de mensajería (Kafka/RabbitMQ) introduce latencias de red, sobrecostos de hardware y complejidad operativa desproporcionada. | **Modelo Monolítico Modular con persistencia única:** La base de datos H2/PostgreSQL maneja holgadamente la carga transaccional y analítica simultánea. |
| **2. Complejidad de Consultas** | Las consultas del Dashboard (conteo por severidad, conteo por estado y cálculo de días promedio de cierre) se resuelven eficientemente mediante índices relacionales, cláusulas `GROUP BY` y funciones nativas de fecha (`DATEDIFF`). No existen requerimientos de búsqueda full-text masiva ni analítica OLAP distribuida que justifiquen una base de datos secundaria especializada (e.g., Elasticsearch o ClickHouse). | **Proyecciones JPQL / SQL Nativas Livianas:** Se extienden los puertos del repositorio con proyecciones directas sobre el mismo esquema. |
| **3. Consistencia** | Los comités de auditoría y oficiales de cumplimiento requieren consultar métricas consolidadas inmediatamente después de registrar o cerrar un hallazgo. Un esquema de CQRS Asíncrono con *Eventual Consistency* (consistencia eventual) provocaría confusión ("acabo de cerrar el hallazgo pero el dashboard no lo refleja"), obligando a manejar mecanismos complejos de polling o WebSockets. | **Consistencia Inmediata (Strong Consistency):** Las lecturas y escrituras comparten la misma transacción ACID garantizando veracidad instantánea. |
| **4. Naturaleza de la Trazabilidad Exigida** | El marco regulatorio y legal exige una pista de auditoría forense inmutable (*quién cambió de estado, cuándo, cuál fue el estado previo y cuál el motivo*). No se requiere la capacidad de reconstruir el estado de la entidad mediante el *replay* secuencial de miles de micro-eventos granulares (*Event Sourcing*), lo cual añadiría sobrecarga de versionado de esquemas y snapshots. | **Bitácora Relacional Append-Only:** Se registra cada transición en la tabla `historial_cambios_estado` como un hecho inmutable dentro de la misma transacción. |
| **5. Señales de Sobre-ingeniería** | Adoptar Event Sourcing y CQRS completo simplemente por tendencia o moda arquitectónica generaría: complejidad accidental, mayor curva de aprendizaje para el equipo, dificultades para consultar el estado actual (necesidad de proyecciones asíncronas) y riesgo de desincronización de datos. | **Extensión Liviana:** Se satisfacen el 100% de los requisitos funcionales y no funcionales con una fracción de la complejidad y costo de mantenimiento. |

---

## 7. Justificación Técnica de las 4 Decisiones de Diseño

### Decisión 1: `Severidad` como Enum Simple vs. `EstadoHallazgo` con Máquina de Estados Finita
- **Justificación:** La severidad (`CRITICA, ALTA, MEDIA, BAJA`) representa una propiedad cualitativa y estática asignada al hallazgo que no gobierna transiciones de comportamiento. En contraste, el estado (`ABIERTO, EN_REMEDIACION, CERRADO, REABIERTO`) define el ciclo de vida del proceso de auditoría. 
- **Implementación:** `EstadoHallazgo` encapsula la máquina de estados a través del método `puedeTransicionarA(EstadoHallazgo destino)`. Esto asegura que ninguna entidad o caso de uso pueda violar las reglas de transición (por ejemplo, pasar de `ABIERTO` a `CERRADO` sin pasar por `EN_REMEDIACION`), centralizando la regla de negocio dentro del núcleo del dominio.

### Decisión 2: `PlanRemediacion` como Value Object Embebido en el Aggregate Root
- **Justificación:** Un plan de remediación no tiene identidad propia independiente; su existencia y validez solo tienen sentido en el contexto del hallazgo al que pertenece. Además, la regla de negocio estipula que para cerrar un hallazgo debe existir obligatoriamente un plan de remediación previo.
- **Implementación:** `PlanRemediacion` se diseña como un `record` inmutable con validación de obligatoriedad en su constructor compacto (`responsable`, `fechaLimite`, `notas`). El aggregate root `HallazgoAuditoria` mantiene la referencia a este Value Object y verifica su presencia dentro del método `cerrar()`, garantizando la consistencia transaccional inmediata en un solo límite de agregado.

### Decisión 3: CQRS Completo vs. Extensión Liviana del Puerto con Proyecciones JPQL
- **Justificación:** El patrón CQRS formal propone separar completamente las bases de datos de lectura y escritura, comunicadas asíncronamente mediante eventos de integración. Para el dominio de auditoría de hallazgos, esto añadiría consistencia eventual, puntos únicos de fallo y complejidad en la sincronización.
- **Implementación:** Se adoptó una extensión liviana en la que `HallazgoRepositoryPort` define métodos especializados para lecturas analíticas (`contarPorSeveridad()`, `contarPorEstado()`, `promedioDiasCierrePorArea()`). En la capa de infraestructura, `HallazgoJpaRepository` implementa estas consultas mediante proyecciones optimizadas (`@Query` con `GROUP BY` y agregaciones), desacoplando los modelos de lectura de los modelos de dominio sin introducir infraestructura redundante.

### Decisión 4: Bitácora Relacional Append-Only vs. Event Store Complejo con Gestión de Snapshots
- **Justificación:** En Event Sourcing puro, el estado de una entidad no se almacena como una fila relacional, sino como una secuencia completa de eventos del pasado (`HallazgoRegistrado`, `RemediacionIniciada`, `HallazgoCerrado`), obligando a reconstruir el objeto mediante *replay* y requiriendo mecanismos de *snapshotting* para evitar degradación de rendimiento con el tiempo.
- **Implementación:** La solución adoptada mantiene la entidad actual en la tabla `hallazgos` y crea una tabla `historial_cambios_estado` estrictamente *append-only* (solo inserciones, sin updates ni deletes). Cada servicio de aplicación (`IniciarRemediacionService`, `CerrarHallazgoService`, `ReabrirHallazgoService`) registra el cambio en el puerto `HistorialAuditoriaPort` dentro del mismo hilo transaccional. Esto cumple cabalmente con la trazabilidad forense exigida por normas internacionales (e.g., ISO 27001, COSO, SOX) con máxima simplicidad y confiabilidad.

---

## 8. Conclusiones Profesionales

1. **La Pureza Arquitectónica Protege el Negocio:** Al mantener `domain` y `usecase` 100% libres de dependencias de Spring Boot y JPA, la lógica de auditoría y las reglas de remediación pueden evolucionar o migrar de framework sin alterar una sola línea del núcleo del negocio.
2. **Pragmatismo sobre Dogmatismo:** Los patrones arquitectónicos como CQRS y Event Sourcing son herramientas para resolver problemas específicos de ultra-alta escala o dominios altamente orientados a eventos (e.g., trading financiero). Aplicarlos indiscriminadamente en sistemas de escala corporativa convencional constituye sobre-ingeniería.
3. **Mantenibilidad y Testeabilidad:** La arquitectura implementada permite que las pruebas de dominio (`HallazgoAuditoriaDomainTest`) se ejecuten en milisegundos sin levantar contextos pesados, mientras que las pruebas de integración (`HallazgoControllerIntegrationTest`) verifican de extremo a extremo los contratos de API y la persistencia en H2.