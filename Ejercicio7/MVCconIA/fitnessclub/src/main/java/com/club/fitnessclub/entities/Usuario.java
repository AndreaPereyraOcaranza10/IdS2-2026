package com.club.fitnessclub.entities;

import com.club.fitnessclub.enums.Rol;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuario del SISTEMA (personal del club), distinto de Persona (socio/familiar).
 * Es lo que autentica Spring Security.
 *
 * <p>La contraseña se guarda SIEMPRE hasheada con BCrypt, nunca en texto plano.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuario", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_username", columnNames = "username"))
public class Usuario extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 60)
    private String username;

    /** Hash BCrypt (60 caracteres); se deja margen en la longitud. */
    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;
}
