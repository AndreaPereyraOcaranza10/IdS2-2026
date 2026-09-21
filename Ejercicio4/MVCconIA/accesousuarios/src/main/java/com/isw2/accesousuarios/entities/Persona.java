package com.isw2.accesousuarios.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * CAPA: MODELO (entidad JPA)
 * ============================================================================
 * Representa a la PERSONA que se registra en el sistema con sus datos
 * personales: Nombre, Apellido, Documento, Fecha de Nacimiento y Correo
 * Personal. El correo es, además, el "usuario" con el que la persona ingresa.
 *
 * Los datos de acceso (clave, intentos fallidos, bloqueo) NO están aquí sino en
 * {@link Usuario}: una Persona tiene UN Usuario (relación 1 a 1).
 *
 * ANOTACIONES DE CLASE
 *   @Entity  : marca la clase como entidad JPA; Hibernate la mapea a una tabla.
 *   @Table   : personaliza la tabla. name="personas" fija el nombre y
 *              uniqueConstraints crea indices ÚNICOS en la base de datos, de modo
 *              que aunque falle una validación de la aplicación (por ejemplo,
 *              dos registros simultaneos) MySQL impide correo o documento repetidos.
 *   @Getter / @Setter (Lombok): generan los getters y setters en compilación.
 *              No se usa @Data en entidades porque genera equals/hashCode/toString
 *              sobre todos los campos, lo que da problemas con proxies de Hibernate
 *              y relaciones.
 *   @NoArgsConstructor (Lombok): constructor sin argumentos. JPA lo EXIGE para
 *              poder instanciar la entidad por reflexión al leer de la base.
 */
@Entity
@Table(name = "personas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_personas_documento", columnNames = "documento"),
        @UniqueConstraint(name = "uk_personas_correo", columnNames = "correo")
})
@Getter
@Setter
@NoArgsConstructor
public class Persona {

    /**
     * Clave primaria (long, consistente con el resto de los proyectos).
     * @Id                : identifica la clave primaria.
     * @GeneratedValue    : IDENTITY delega en MySQL la generación (AUTO_INCREMENT).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** Nombre de pila. @Column define restricciones a nivel de columna: NOT NULL y largo máximo. */
    @Column(nullable = false, length = 60)
    private String nombre;

    /** Apellido. */
    @Column(nullable = false, length = 60)
    private String apellido;

    /** Documento de identidad. Es ÚNICO (ver @UniqueConstraint de la clase). */
    @Column(nullable = false, length = 20)
    private String documento;

    /**
     * Fecha de nacimiento. LocalDate (java.time) se mapea a una columna DATE de MySQL.
     * name="fecha_nacimiento" fija el nombre de la columna en snake_case.
     */
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    /**
     * Correo personal. Funciona como NOMBRE DE USUARIO del sistema, por eso es
     * ÚNICO. Se guarda normalizado (sin espacios y en minúsculas).
     */
    @Column(nullable = false, length = 120)
    private String correo;
}
