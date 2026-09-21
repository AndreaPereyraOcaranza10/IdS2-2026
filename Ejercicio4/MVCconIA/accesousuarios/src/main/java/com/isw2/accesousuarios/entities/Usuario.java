package com.isw2.accesousuarios.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CAPA: MODELO (entidad JPA)
 * ============================================================================
 * Representa las CREDENCIALES DE ACCESO de una {@link Persona}: la clave (con
 * hash BCrypt), la cantidad de intentos fallidos consecutivos y si la cuenta
 * está bloqueada.
 *
 * El NOMBRE DE USUARIO no se repite aquí: es el correo de la Persona asociada
 * (persona.correo), tal como pide el enunciado.
 *
 * Relación: Persona 1 --- 1 Usuario (Usuario es el lado propietario y guarda la
 * clave foranea persona_id).
 *
 * ANOTACIONES DE CLASE
 *   @Entity, @Table, @Getter, @Setter, @NoArgsConstructor: ver {@link Persona}.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    /** Clave primaria autoincremental (IDENTITY). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Clave con hash BCrypt (nunca en texto plano). Un hash BCrypt mide 60
     * caracteres; se reserva length=100 por holgura.
     */
    @Column(nullable = false, length = 100)
    private String clave;

    /**
     * Cantidad de claves incorrectas CONSECUTIVAS. Se incrementa en cada error y
     * vuelve a 0 con un ingreso exitoso. Al llegar a 3 la cuenta se bloquea.
     */
    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    /** true = cuenta bloqueada por superar los intentos fallidos permitidos. */
    @Column(nullable = false)
    private boolean bloqueado;

    /**
     * Persona duena de este usuario.
     *
     * @OneToOne  : relación uno a uno.
     *   - fetch = LAZY   : la Persona se carga recien cuando se la usa (evita un
     *                      JOIN innecesario). Como open-in-view está desactivado,
     *                      solo se accede a ella DENTRO de la capa de servicio.
     *   - optional=false : un Usuario no puede existir sin Persona.
     *   - cascade=PERSIST: al guardar un Usuario NUEVO se guarda también su Persona
     *                      en la misma operación (una sola llamada a save).
     * @JoinColumn: define la columna foranea "persona_id" en la tabla usuarios.
     *   - unique=true    : garantiza el "uno a uno" (una persona, un usuario).
     *   - nullable=false : la FK es obligatoria.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "persona_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_usuarios_persona"))
    private Persona persona;
}
