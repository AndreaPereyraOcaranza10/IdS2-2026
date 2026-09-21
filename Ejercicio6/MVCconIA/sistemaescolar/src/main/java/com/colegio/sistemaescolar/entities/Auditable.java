package com.colegio.sistemaescolar.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * SUPERCLASE de TODAS las entidades: aporta el identificador, la BAJA LÓGICA y la AUDITORÍA.
 *
 * <h2>¿Qué es la auditoría de entidades?</h2>
 * Registrar automáticamente QUIÉN y CUÁNDO creó o modificó cada registro. Se implementa con
 * Spring Data JPA Auditing:
 * <ol>
 *   <li>{@code @EnableJpaAuditing} (ver config/ConfiguracionAuditoria) activa la función.</li>
 *   <li>{@code @EntityListeners(AuditingEntityListener.class)} hace que, antes de cada INSERT/UPDATE,
 *       Spring complete los campos anotados con {@code @CreatedDate}, {@code @CreatedBy},
 *       {@code @LastModifiedDate} y {@code @LastModifiedBy}.</li>
 *   <li>El "quién" lo entrega el bean {@code AuditorAware} (usuario autenticado, o "sistema").</li>
 * </ol>
 *
 * <h2>Baja lógica</h2>
 * Los registros no se borran físicamente: {@code eliminado = true}. Así se conserva la historia
 * (por ejemplo las notas de un alumno dado de baja) y la auditoría registra quién dio la baja.
 * Los repositorios filtran con {@code ...EliminadoFalse...}.
 *
 * <h2>Anotaciones</h2>
 * <ul>
 *   <li>{@code @MappedSuperclass}: sus campos se agregan a las tablas de las entidades hijas
 *       (no tiene tabla propia).</li>
 *   <li>{@code @Id @GeneratedValue(IDENTITY)}: clave primaria autoincremental de MySQL
 *       (convención del proyecto: {@code long id}, no UUID).</li>
 *   <li>{@code @Getter/@Setter} (Lombok): generan los accesores.</li>
 * </ul>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class Auditable {

    /** Clave primaria. El valor 0 indica "entidad nueva, todavía no guardada". */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** Baja lógica: true = el registro se considera borrado. */
    @Column(nullable = false)
    private boolean eliminado = false;

    /** Fecha y hora de creación (se completa sola; no se puede modificar después). */
    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Usuario que creó el registro (email del usuario autenticado, o "sistema"). */
    @CreatedBy
    @Column(name = "creado_por", length = 150, updatable = false)
    private String creadoPor;

    /** Fecha y hora de la última modificación. */
    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /** Usuario que realizó la última modificación. */
    @LastModifiedBy
    @Column(name = "modificado_por", length = 150)
    private String modificadoPor;
}
