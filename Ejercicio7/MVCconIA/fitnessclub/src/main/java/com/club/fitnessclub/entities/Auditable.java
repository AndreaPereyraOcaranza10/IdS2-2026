package com.club.fitnessclub.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Clase base de AUDITORÍA para todas las entidades del dominio.
 *
 * <ul>
 *   <li>@MappedSuperclass: NO es una entidad ni tiene tabla propia. Sus campos se
 *       copian como columnas en la tabla de CADA entidad hija.</li>
 *   <li>@EntityListeners(AuditingEntityListener.class): engancha el ciclo de vida JPA
 *       (@PrePersist/@PreUpdate) para completar los campos @Created* / @LastModified*.
 *       Requiere @EnableJpaAuditing (ver ConfiguracionAuditoria).</li>
 * </ul>
 *
 * <p>También incluye "activo" para BAJA LÓGICA: en vez de borrar filas (que rompería
 * el historial de accesos y pagos) se marca activo=false, y el registro conserva
 * quién y cuándo lo modificó por última vez.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    /** Fecha/hora de creación. updatable=false: nunca se reescribe. */
    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    /** Fecha/hora de la última modificación. */
    @LastModifiedDate
    @Column(name = "modificado_en")
    private LocalDateTime modificadoEn;

    /** Usuario que creó el registro (username). */
    @CreatedBy
    @Column(name = "creado_por", updatable = false, length = 60)
    private String creadoPor;

    /** Usuario que hizo la última modificación (username). */
    @LastModifiedBy
    @Column(name = "modificado_por", length = 60)
    private String modificadoPor;

    /** Baja lógica: true = vigente, false = dado de baja. */
    @Column(nullable = false)
    private boolean activo = true;
}
