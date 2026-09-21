package com.club.fitnessclub.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una visita al club: horario de entrada y, cuando corresponde, de salida.
 *
 * <p>Modelado: una fila por visita.
 * <ul>
 *   <li>Al ingresar: se crea con fechaHoraEntrada y fechaHoraSalida = null.</li>
 *   <li>Al salir: se completa fechaHoraSalida del registro abierto.</li>
 * </ul>
 * "Registro abierto" (salida null) = la persona está actualmente dentro.
 * La auditoría (creado_por) deja constancia de qué usuario de recepción lo cargó.
 *
 * <p>Índice por persona: la consulta más frecuente es "¿tiene esta persona una
 * visita abierta?" y conviene que sea rápida (también se estresa en las pruebas de carga).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "registro_acceso", indexes = @Index(name = "idx_acceso_persona_salida", columnList = "persona_id, fecha_hora_salida"))
public class RegistroAcceso extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Column(name = "fecha_hora_entrada", nullable = false)
    private LocalDateTime fechaHoraEntrada;

    /** null mientras la persona siga dentro del club. */
    @Column(name = "fecha_hora_salida")
    private LocalDateTime fechaHoraSalida;

    /** true si todavía no registró la salida. */
    public boolean estaAdentro() {
        return fechaHoraSalida == null;
    }
}
