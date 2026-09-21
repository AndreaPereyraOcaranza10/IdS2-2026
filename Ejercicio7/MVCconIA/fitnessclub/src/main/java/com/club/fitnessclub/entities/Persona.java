package com.club.fitnessclub.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.club.fitnessclub.enums.Parentesco;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persona registrada en el club: el socio titular o un familiar.
 * Todas comparten datos, foto de rostro y registro de accesos.
 *
 * <p>Relaciones:
 * <ul>
 *   <li>N Persona -> 1 GrupoFamiliar (lado propietario: tiene la FK).</li>
 *   <li>1 Persona -> 0..1 ImagenRostro (FK "imagen_rostro_id" en esta tabla).</li>
 *   <li>1 Persona -> N RegistroAcceso (lado inverso).</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "persona", uniqueConstraints = @UniqueConstraint(name = "uk_persona_dni", columnNames = "dni"))
public class Persona extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    /** DNI único: identifica a la persona en la recepción del club. */
    @Column(nullable = false, length = 15)
    private String dni;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 120)
    private String email;

    @Column(length = 30)
    private String telefono;

    /** Rol dentro de la familia. EnumType.STRING: guarda el nombre, no la posición (más seguro si se reordena el enum). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Parentesco parentesco;

    /**
     * Lado propietario de la relación con la familia.
     * LAZY: no se trae el grupo salvo que se necesite. optional=false: toda persona pertenece a un grupo.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_familiar_id", nullable = false)
    private GrupoFamiliar grupoFamiliar;

    /**
     * Foto del rostro, en entidad aparte y LAZY: los listados de personas NO
     * descargan los bytes de la imagen. orphanRemoval: reemplazar la foto borra la anterior.
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "imagen_rostro_id")
    private ImagenRostro imagenRostro;

    /** Historial de entradas/salidas. Sin cascade: los accesos se crean desde ServicioAcceso. */
    @OneToMany(mappedBy = "persona")
    private List<RegistroAcceso> accesos = new ArrayList<>();

    /** Utilidad para mostrar "Apellido, Nombre" en vistas y DTOs. */
    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }
}
