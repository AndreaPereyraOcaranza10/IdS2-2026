package com.club.fitnessclub.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de solo lectura de un RegistroAcceso (entrada/salida). Aplana los datos de la persona y
 * de su familia para que la vista de recepción no navegue relaciones de entidades.
 */
@Data
@NoArgsConstructor
public class RegistroAccesoDTO implements Serializable {

    /** Serializable porque se guarda como atributo flash en la sesión HTTP (redirect tras registrar entrada/salida). */
    private static final long serialVersionUID = 1L;

    private long id;
    private long personaId;
    private String personaNombre;
    private String personaDni;
    private String grupoFamiliarNombre;
    private boolean tieneRostro;

    private LocalDateTime fechaHoraEntrada;
    /** null mientras la persona siga dentro. */
    private LocalDateTime fechaHoraSalida;

    /** true si todavía no registró la salida. */
    private boolean adentro;

    /** Tiempo de permanencia legible ("1 h 20 min"); en visitas abiertas, el tiempo transcurrido hasta ahora. */
    private String permanencia;

    /**
     * Advertencia NO bloqueante para recepción (p. ej. "la familia tiene una cuota vencida").
     * La completa el servicio solo al registrar la entrada.
     */
    private String advertencia;
}
