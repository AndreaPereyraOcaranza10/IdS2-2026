# Plan de pruebas: unitarias, de carga y de stress

## 1. Pruebas unitarias (JUnit 5 + Mockito + AssertJ)

**Objetivo:** verificar cada regla de negocio de la capa de servicio de forma aislada, sin base de datos
ni contexto de Spring (los repositorios se reemplazan por dobles de prueba de Mockito). Cada test corre en milisegundos.

**Ubicación:** `src/test/java/com/club/fitnessclub/` — **Ejecutar:** `mvn test`

| Clase de prueba | Qué verifica |
|---|---|
| `ServicioPersonaTest` | DNI único; familia vigente; titular único y primero; foto JPEG/PNG ≤ 3 MB con validación de firma (magic numbers); foto obligatoria en el alta; baja del titular. |
| `ServicioGrupoFamiliarTest` | Cálculo de titular e integrantes; baja de familia con cuotas pendientes (rechazada) y en cascada a integrantes. |
| `ServicioAccesoTest` | Entrada/salida con reloj fijo; visita abierta duplicada; salida sin entrada; DNI inválido/inexistente; advertencia por cuota vencida (no bloquea). |
| `ServicioCuotaTest` | Una cuota por familia y período; vencimiento ≥ período; emisión masiva omite existentes; anulación solo sin pagos; importe ≥ lo pagado. |
| `ServicioPagoTest` | Pago total/parcial/combinado (3 medios); no supera el saldo; comprobante obligatorio en Transferencia y Mercado Pago; cuota anulada/pagada; anular pago revierte a PENDIENTE. |
| `ServicioUsuarioTest` | Contraseña hasheada (BCrypt), obligatoria en alta y opcional en edición; confirmación; último ADMIN protegido. |
| `MapeadoresTest` | Conversión de período (YearMonth ↔ primer día del mes), texto del período, duración y cuota vencida. |

**Técnicas aplicadas:** patrón Arrange-Act-Assert, `Clock` inyectado (`DatosDePrueba.RELOJ`: hoy = 15/09/2026) para
resultados deterministas, y `DatosDePrueba` como fábrica de entidades.

## 2. Prueba de CARGA (comportamiento con la demanda esperada)

**Objetivo:** confirmar que el sistema cumple los tiempos de respuesta con la cantidad de usuarios concurrentes esperada
en horas pico (recepción registrando ingresos, más algunas consultas del administrador).

**Herramienta:** Apache JMeter 5.6+ — plan `pruebas/jmeter/plan-carga-stress.jmx` (datos: `dnis.csv`).

**Escenario de cada usuario virtual** (login una sola vez; luego repite en bucle con 0,5–1 s de espera entre acciones):
1. `GET /login` (extrae token CSRF) → `POST /login` como `recepcion`.
2. `GET /panel` → `GET /accesos` → `POST /accesos/entrada` (DNI del CSV) → `GET /accesos` → `POST /accesos/salida` → `GET /personas`.

Cada paso lleva una **aserción de contenido** (no solo código HTTP): valida que la página devuelta sea la esperada.

**Preparación:**
1. Vaciar la base (`DROP DATABASE fitnessclub;`) y arrancar con datos de prueba suficientes:
   `mvn spring-boot:run -Dspring-boot.run.arguments=--club.datos-demo.cantidad-familias=200` (600 personas, DNIs 30000001–30000600).
2. Ejecutar JMeter **en otra máquina** si es posible (para no competir por CPU con la aplicación).

**Comando (no GUI):**
```
cd pruebas/jmeter
jmeter -n -t plan-carga-stress.jmx -Jusuarios=50 -Jrampa=60 -Jduracion=300 -l carga.jtl -e -o reporte-carga
```

**Criterios de aceptación:**
| Métrica | Objetivo |
|---|---|
| Tasa de errores (HTTP + aserciones) | < 1 % |
| Tiempo de respuesta p95 (páginas) | < 800 ms |
| Tiempo de respuesta p95 (POST entrada/salida) | < 500 ms |
| CPU del servidor de la aplicación | < 75 % sostenido |

## 3. Prueba de STRESS (buscar el punto de quiebre)

**Objetivo:** llevar el sistema más allá de la carga esperada para encontrar **dónde y cómo falla** y comprobar que se
**recupera** al bajar la demanda.

**Procedimiento:** repetir el mismo plan aumentando escalonadamente los usuarios concurrentes; cada escalón dura 3 minutos:

| Escalón | `-Jusuarios` | `-Jrampa` | `-Jduracion` |
|---|---|---|---|
| 1 | 100 | 30 | 180 |
| 2 | 200 | 30 | 180 |
| 3 | 400 | 60 | 180 |
| 4 | 800 | 60 | 180 |

```
jmeter -n -t plan-carga-stress.jmx -Jusuarios=200 -Jrampa=30 -Jduracion=180 -l stress-200.jtl -e -o reporte-stress-200
```
Para acelerar el ritmo (más presión): `-Jpensar_ms=0 -Jvariacion_ms=0`.

**Se considera "punto de quiebre"** el primer escalón donde ocurra alguno de: errores > 5 %, p95 > 3 s, o timeouts.
Luego se ejecuta un escalón de **recuperación** (`-Jusuarios=20`) para verificar que el sistema vuelve a los tiempos normales.

**Qué observar y posibles cuellos de botella esperados:**
- **Pool de conexiones a MySQL** (HikariCP, 10 por defecto): si se agota, aumentan las esperas. Ajuste posible: `spring.datasource.hikari.maximum-pool-size`.
- **Login con BCrypt** (costoso a propósito): por eso el plan inicia sesión una sola vez por usuario virtual.
- **Consulta de "visita abierta"** por persona: cubierta por el índice `idx_acceso_persona_salida`.
- **Condición de carrera conocida:** dos entradas simultáneas del mismo DNI podrían dejar dos visitas abiertas (no hay restricción única en BD). Con el CSV compartido por todos los hilos aparece en el stress como respuestas "ya tiene una entrada registrada" (no son errores HTTP). Mitigación futura: bloqueo optimista/pesimista sobre la persona o índice único parcial.
- Memoria/GC de la JVM y saturación de hilos de Tomcat (`server.tomcat.threads.max`, 200 por defecto).

**Registro de resultados sugerido** (completar al ejecutar): usuarios, throughput (req/s), p50/p95/p99, % errores, CPU/RAM de app y BD, observaciones.
