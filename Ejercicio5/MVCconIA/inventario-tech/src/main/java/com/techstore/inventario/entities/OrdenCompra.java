package com.techstore.inventario.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de una orden de compra a un proveedor.
 * El total y las lineas (detalles) se gestionan siempre a traves de
 * ServicioOrdenCompra, nunca se arman "a mano" desde el controller.
 */
@Entity
@Table(name = "ordenes_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador de negocio visible al usuario, formato OC-yyyyMMdd-NN. */
    @Column(nullable = false, unique = true, length = 20)
    private String numeroOrden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    /** Usuario que genero la orden (auditoria). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Se completa recien cuando la orden pasa a CONFIRMADA. */
    private LocalDateTime fechaConfirmacion;

    /** Se completa recien cuando la orden pasa a ANULADA. */
    private LocalDateTime fechaAnulacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrden estado = EstadoOrden.BORRADOR;

    /** Suma de los subtotales de todas las lineas. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrdenCompra> detalles = new ArrayList<>();

    /**
     * Agrega una linea de detalle manteniendo sincronizada la relacion
     * bidireccional (JPA no lo hace automaticamente).
     */
    public void agregarDetalle(DetalleOrdenCompra detalle) {
        detalles.add(detalle);
        detalle.setOrdenCompra(this);
    }
}
