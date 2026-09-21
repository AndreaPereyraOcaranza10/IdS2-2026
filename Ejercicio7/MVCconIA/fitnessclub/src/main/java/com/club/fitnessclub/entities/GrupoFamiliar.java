package com.club.fitnessclub.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Grupo familiar del club. Es la unidad a la que se le factura la cuota:
 * el pago se registra POR FAMILIA, no por persona.
 *
 * <p>Relaciones:
 * <ul>
 *   <li>1 GrupoFamiliar -> N Persona (integrantes). Lado inverso (mappedBy): la FK
 *       "grupo_familiar_id" vive en la tabla persona.</li>
 *   <li>1 GrupoFamiliar -> N Cuota.</li>
 * </ul>
 *
 * <p>El TITULAR no es un campo: es la Persona con parentesco TITULAR. El servicio
 * garantiza que exista exactamente uno (evita una FK circular grupo<->persona).
 *
 * <p>Se usa @Getter/@Setter en lugar de @Data a propósito: @Data genera
 * toString/equals/hashCode que recorren las colecciones y disparan cargas LAZY
 * o recursión infinita en relaciones bidireccionales.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "grupo_familiar")
public class GrupoFamiliar extends Auditable {

    /** Convención del proyecto: id numérico con estrategia IDENTITY (AUTO_INCREMENT de MySQL). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** Nombre descriptivo, p. ej. "Familia Pérez". */
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 150)
    private String direccion;

    @Column(length = 30)
    private String telefono;

    /**
     * Integrantes de la familia.
     * cascade = ALL: guardar el grupo guarda a sus personas.
     * orphanRemoval = true: quitar una persona de la lista elimina su fila.
     * LAZY (por defecto en @OneToMany): no se cargan hasta que el servicio las pida.
     */
    @OneToMany(mappedBy = "grupoFamiliar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Persona> integrantes = new ArrayList<>();

    /** Cuotas emitidas a la familia. Sin cascade: las cuotas se crean desde ServicioCuota. */
    @OneToMany(mappedBy = "grupoFamiliar")
    private List<Cuota> cuotas = new ArrayList<>();

    /**
     * Método de conveniencia para mantener sincronizados AMBOS lados de la relación
     * bidireccional (si solo se agrega a la lista, persona.grupoFamiliar queda null
     * y la FK no se guarda).
     */
    public void agregarIntegrante(Persona persona) {
        integrantes.add(persona);
        persona.setGrupoFamiliar(this);
    }
}
