# Diagrama de clases rediseñado

Se agregó el módulo de **cuotas y pagos por familia** (medios: Efectivo, Transferencia, Mercado Pago), la
**auditoría** de todas las entidades y el **usuario del sistema** (seguridad).
Para verlo: pegar el bloque en <https://mermaid.live> o en draw.io (Extras → Editar diagrama → Mermaid).

```mermaid
classDiagram
    class Auditable {
        <<abstract>>
        -LocalDateTime creadoEn
        -LocalDateTime modificadoEn
        -String creadoPor
        -String modificadoPor
        -boolean activo
    }
    class GrupoFamiliar {
        -long id
        -String nombre
        -String direccion
        -String telefono
        +agregarIntegrante(Persona)
    }
    class Persona {
        -long id
        -String nombre
        -String apellido
        -String dni
        -LocalDate fechaNacimiento
        -String email
        -String telefono
        -Parentesco parentesco
        +getNombreCompleto() String
    }
    class ImagenRostro {
        -long id
        -String nombreArchivo
        -String contentType
        -byte[] datos
    }
    class RegistroAcceso {
        -long id
        -LocalDateTime fechaHoraEntrada
        -LocalDateTime fechaHoraSalida
        +estaAdentro() boolean
    }
    class Cuota {
        -long id
        -LocalDate periodo
        -BigDecimal importe
        -LocalDate fechaVencimiento
        -EstadoCuota estado
        +getMontoPagado() BigDecimal
        +getSaldo() BigDecimal
        +actualizarEstado()
    }
    class Pago {
        -long id
        -BigDecimal monto
        -LocalDateTime fechaPago
        -MedioPago medioPago
        -String referencia
    }
    class Usuario {
        -long id
        -String username
        -String password
        -Rol rol
    }
    class Parentesco { <<enumeration>> TITULAR CONYUGE HIJO OTRO }
    class MedioPago { <<enumeration>> EFECTIVO TRANSFERENCIA MERCADO_PAGO }
    class EstadoCuota { <<enumeration>> PENDIENTE PAGADA ANULADA }
    class Rol { <<enumeration>> ADMIN RECEPCION }

    Auditable <|-- GrupoFamiliar
    Auditable <|-- Persona
    Auditable <|-- RegistroAcceso
    Auditable <|-- Cuota
    Auditable <|-- Pago
    Auditable <|-- Usuario
    GrupoFamiliar "1" *-- "1..*" Persona : integrantes
    Persona "1" *-- "0..1" ImagenRostro : rostro
    Persona "1" <-- "0..*" RegistroAcceso : accesos
    GrupoFamiliar "1" <-- "0..*" Cuota : cuotas
    Cuota "1" *-- "0..*" Pago : pagos
    Persona ..> Parentesco
    Pago ..> MedioPago
    Cuota ..> EstadoCuota
    Usuario ..> Rol
```

## Decisiones de diseño

| Decisión | Justificación |
|---|---|
| `Persona` única con `Parentesco` | Entrada/salida y foto aplican a cualquier persona; el titular se deriva del parentesco. |
| `ImagenRostro` separada y LAZY | Los listados no descargan los bytes de las fotos. |
| `RegistroAcceso` con salida nullable | Una fila por visita; salida `null` = la persona está adentro. |
| `Cuota` por familia y período, 1–* `Pago` | Permite pagar con uno o varios medios; la cuota pasa a PAGADA al cubrir el importe. |
| `MedioPago` enum + `referencia` | Los 3 medios comparten atributos; herencia sería sobreingeniería. Mercado Pago: se registra el ID de operación (sin integración real con la API). |
| Auditoría con Spring Data JPA + baja lógica | Conserva historial y registra quién/cuándo creó y modificó. |
| Pagos inmutables (solo se anulan) | Trazabilidad contable. |
