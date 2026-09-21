# Club Fitness — Sistema de gestión de club deportivo

 · Ingeniería de Software II · 

Sistema web para un club deportivo: registro de **socios y su grupo familiar** (con **foto de rostro**),
**horario de entrada y salida** de cada persona, y **cobro de la cuota familiar** con distintos medios de pago
(Efectivo, Transferencia, Mercado Pago). Incluye auditoría de entidades, seguridad por roles y pruebas.

## Stack
Java 17 · Spring Boot 3.3 · Spring MVC + **Thymeleaf** · Spring Data JPA/Hibernate (**ORM**) · **MySQL** ·
Spring Security 6 · Bean Validation · Lombok · JUnit 5 + Mockito · JMeter.
Vistas: plantilla **Health & Fitness** (Bootstrap 4, sitio público) + **SB Admin** (Bootstrap 5, panel). Ver `docs/evaluacion-plantilla.md`.

## Cómo ejecutarlo
1. Requisitos: JDK 17+, Maven 3.9+, MySQL (XAMPP). Iniciar MySQL en el puerto 3306.
2. Revisar usuario/clave de MySQL en `src/main/resources/application.properties` (por defecto `root` sin clave; la base `fitnessclub` se crea sola).
3. `mvn spring-boot:run` → abrir <http://localhost:8080>
4. Usuarios de demostración (se crean al primer arranque; **cambiar las claves**):

| Usuario | Clave | Rol | Accede a |
|---|---|---|---|
| `admin` | `admin1234` | ADMIN | Todo |
| `recepcion` | `recepcion1234` | RECEPCION | Tablero, entradas/salidas, listado de personas |

Al primer arranque también se crean 5 familias de demostración (DNIs 30000001 a 30000015). Cantidad configurable con `club.datos-demo.cantidad-familias`.

Tests unitarios: `mvn test` · Carga y stress: ver `docs/plan-de-pruebas.md`.

## Arquitectura en capas (MVC + Servicio + Persistencia)
```
Navegador ─HTTP─> [VISTA] templates/*.html (Thymeleaf)         → solo ve DTOs
                  [CONTROLADOR] controllers/Controlador*       → @Controller, @Valid sobre DTOs
                  [SERVICIO] services/Servicio*                → @Service, reglas de negocio, @Transactional
                  [MAPEADOR] mappers/Mapeador*                 → Entidad <-> DTO
                  [REPOSITORIO] repositories/Repositorio*      → Spring Data JPA
                  [MODELO] entities/*                          → @Entity → tablas MySQL
```
Otros paquetes: `dtos` (objetos de transferencia), `enums`, `exceptions` (`ExcepcionNegocio`), `config` (auditoría, seguridad, datos iniciales), `security` (UserDetailsService).

**Regla de oro:** las entidades JPA nunca llegan al controlador ni a la vista; entre capas solo viajan **DTOs**.
`spring.jpa.open-in-view=false` lo refuerza: una entidad con relaciones LAZY que llegara a la vista fallaría en lugar de filtrarse.

### Conceptos aplicados (cada clase está comentada en detalle)
| Concepto | Dónde |
|---|---|
| MVC | `controllers/`, `templates/`, `entities/` |
| Thymeleaf (layouts con fragmentos, formularios, validación, `sec:authorize`) | `templates/`, `fragments/` |
| ORM/JPA (relaciones 1–1, 1–N, N–1, LAZY, cascade, `@Lob`, índices, restricciones únicas) | `entities/` |
| DTO + mapeadores | `dtos/`, `mappers/` |
| Validación (Bean Validation) + reglas de negocio | `dtos/`, `services/` |
| **Auditoría de entidades** (`@CreatedBy`, `@LastModifiedDate`…, baja lógica) | `entities/Auditable`, `config/ConfiguracionAuditoria` |
| **Seguridad** (login, BCrypt, roles, CSRF, `SecurityFilterChain`) | `config/ConfiguracionSeguridad`, `security/`, `entities/Usuario` |
| Transacciones (`@Transactional(rollbackFor = Exception.class)`) | `services/` |
| Pruebas unitarias / carga / stress | `src/test/`, `pruebas/`, `docs/plan-de-pruebas.md` |

## Funcionalidades
- **Familias y personas**: alta/edición/baja lógica; el primer integrante es el titular; DNI único; **foto de rostro** (JPEG/PNG ≤ 3 MB, validada por contenido).
- **Recepción**: registro de entrada/salida por DNI; muestra la **foto** para verificar identidad; lista de personas dentro; advertencia si la familia tiene cuotas vencidas.
- **Cuotas y pagos**: emisión individual o masiva por período; pago total, parcial o combinado con **Efectivo / Transferencia / Mercado Pago** (los electrónicos exigen comprobante); estado automático PENDIENTE → PAGADA; anulación de pagos.
- **Usuarios**: ABM con contraseñas BCrypt y protección del último administrador.
- **Tablero** con contadores y recaudación del mes (solo ADMIN).

## Decisiones y limitaciones conocidas
- Mercado Pago: se registra el **ID de la operación**; no hay integración real con su API.
- `ddl-auto=update` es solo para desarrollo; en producción usar migraciones (Flyway/Liquibase).
- Dos entradas simultáneas del mismo DNI podrían crear dos visitas abiertas (ver `docs/plan-de-pruebas.md`).
- Las fotos se guardan en la base (`MEDIUMBLOB`); con miles de personas convendría un almacenamiento de archivos/objetos.

## Créditos y licencias
Plantilla pública: *Health & Fitness* de [Colorlib](https://colorlib.com) (crédito conservado en el pie).
Panel: [SB Admin](https://startbootstrap.com/template/sb-admin) © Start Bootstrap (MIT, ver `static/recursos/admin/LICENSE-sb-admin.txt`).
Bootstrap 5 (MIT), Font Awesome Free (CC BY 4.0 / SIL OFL / MIT), simple-datatables (MIT).
