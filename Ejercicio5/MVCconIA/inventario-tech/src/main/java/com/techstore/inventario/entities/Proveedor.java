package com.techstore.inventario.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Proveedor mayorista al que se le realizan ordenes de compra. */
@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String razonSocial;

    /** Identificador de negocio; inmutable una vez creado (ver ServicioProveedor). */
    @Column(nullable = false, unique = true, length = 20)
    private String cuit;

    @Column(length = 30)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String direccion;

    @Column(nullable = false)
    private boolean activo = true;
}
