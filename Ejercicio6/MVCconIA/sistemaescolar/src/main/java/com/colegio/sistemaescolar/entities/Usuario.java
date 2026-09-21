package com.colegio.sistemaescolar.entities;

import com.colegio.sistemaescolar.enums.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * USUARIO del sistema (credenciales de acceso).
 *
 * <ul>
 *   <li>El nombre de usuario ES el correo personal del docente ({@code email}), como pide el enunciado.</li>
 *   <li>La contraseña NUNCA se guarda en texto plano: se guarda el hash BCrypt ({@code passwordHash}).</li>
 *   <li>Un Docente tiene exactamente un Usuario (ver {@link Docente#getUsuario()}); el administrador
 *       inicial es un Usuario sin Docente.</li>
 *   <li>{@code habilitado = false} impide iniciar sesión (cuenta pendiente de aprobación o inhabilitada).</li>
 * </ul>
 */
@Entity
@Table(name = "usuarios",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuarios_email", columnNames = "email"))
@Getter
@Setter
public class Usuario extends Auditable {

    /** Correo = nombre de usuario. Se guarda normalizado en minúsculas. */
    @Column(nullable = false, length = 150)
    private String email;

    /** Hash BCrypt de la contraseña (60 caracteres). */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Rol rol;

    @Column(nullable = false)
    private boolean habilitado = true;

    /** Última vez que se cambió la contraseña (informativo). */
    @Column(name = "ultimo_cambio_password")
    private LocalDateTime ultimoCambioPassword;
}
