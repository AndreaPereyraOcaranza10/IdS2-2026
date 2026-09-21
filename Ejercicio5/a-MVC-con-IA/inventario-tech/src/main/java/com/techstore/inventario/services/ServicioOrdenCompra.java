package com.techstore.inventario.services;

import com.techstore.inventario.entities.*;
import com.techstore.inventario.repositories.RepositorioOrdenCompra;
import com.techstore.inventario.repositories.RepositorioProducto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio de Ordenes de Compra: es el unico punto del sistema donde el
 * stock de Producto se modifica, a traves de confirmar()/anular().
 * Ciclo de vida de una orden: BORRADOR -> CONFIRMADA -> (opcionalmente) ANULADA.
 * BORRADOR tambien puede pasar directo a ANULADA (nunca llego a impactar stock).
 */
@Service
public class ServicioOrdenCompra implements ServicioBase<OrdenCompra> {

    @Autowired
    private RepositorioOrdenCompra repositorioOrdenCompra;

    @Autowired
    private RepositorioProducto repositorioProducto;

    /** Formato de fecha usado en el numero de orden: yyyyMMdd. */
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Genera el proximo numero de orden del dia con formato OC-yyyyMMdd-NN,
     * reiniciando el contador cada dia (NN vuelve a 01 al dia siguiente).
     */
    private String generarNumeroOrden() {
        String fechaHoy = LocalDateTime.now().format(FORMATO_FECHA);
        String prefijo = "OC-" + fechaHoy + "-";
        long cantidadHoy = repositorioOrdenCompra.countByNumeroOrdenStartingWith(prefijo);
        long siguiente = cantidadHoy + 1;
        return prefijo + String.format("%02d", siguiente);
    }

    /**
     * Crea una nueva orden de compra en estado BORRADOR, calculando subtotales
     * de cada linea y el total general. Todavia NO impacta el stock.
     */
    @Transactional
    public OrdenCompra crearBorrador(Proveedor proveedor, Usuario usuario, List<DetalleOrdenCompra> detalles) throws Exception {
        if (detalles == null || detalles.isEmpty()) {
            throw new Exception("La orden debe tener al menos una linea de detalle.");
        }

        OrdenCompra orden = new OrdenCompra();
        orden.setNumeroOrden(generarNumeroOrden());
        orden.setProveedor(proveedor);
        orden.setUsuario(usuario);
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setEstado(EstadoOrden.BORRADOR);

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleOrdenCompra detalle : detalles) {
            // El precio unitario se toma tal cual viene en el detalle (precio de compra
            // de ESTA transaccion), no el precioCompra actual del producto, que puede variar.
            BigDecimal subtotal = detalle.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(detalle.getCantidad()));
            detalle.setSubtotal(subtotal);
            orden.agregarDetalle(detalle); // mantiene sincronizada la relacion bidireccional
            total = total.add(subtotal);
        }
        orden.setTotal(total);

        return repositorioOrdenCompra.save(orden);
    }

    /**
     * Confirma una orden en BORRADOR: suma la cantidad de cada linea al stock
     * del producto correspondiente y marca la orden como CONFIRMADA.
     * Se ejecuta en una unica transaccion para evitar sumas parciales si algo falla a mitad de camino.
     */
    @Transactional
    public OrdenCompra confirmar(long id) throws Exception {
        OrdenCompra orden = findById(id);

        // Solo se puede confirmar desde BORRADOR, para no volver a sumar stock
        // si alguien reintenta confirmar una orden ya CONFIRMADA o ANULADA.
        if (orden.getEstado() != EstadoOrden.BORRADOR) {
            throw new Exception("Solo se puede confirmar una orden en estado BORRADOR. Estado actual: " + orden.getEstado());
        }

        for (DetalleOrdenCompra detalle : orden.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStockActual(producto.getStockActual() + detalle.getCantidad());
            repositorioProducto.save(producto);
        }

        orden.setEstado(EstadoOrden.CONFIRMADA);
        orden.setFechaConfirmacion(LocalDateTime.now());
        return repositorioOrdenCompra.save(orden);
    }

    /**
     * Anula una orden. Si estaba CONFIRMADA, revierte el stock que habia sumado
     * (resta cantidad por cantidad). Si estaba en BORRADOR, no habia impactado
     * stock todavia, asi que solo cambia el estado.
     */
    @Transactional
    public OrdenCompra anular(long id) throws Exception {
        OrdenCompra orden = findById(id);

        if (orden.getEstado() == EstadoOrden.ANULADA) {
            throw new Exception("La orden ya se encuentra anulada.");
        }

        if (orden.getEstado() == EstadoOrden.CONFIRMADA) {
            for (DetalleOrdenCompra detalle : orden.getDetalles()) {
                Producto producto = detalle.getProducto();
                int nuevoStock = producto.getStockActual() - detalle.getCantidad();

                // Proteccion de integridad: si el stock actual ya bajo por debajo de lo
                // que esta orden aporto (por ejemplo, porque ya se vendio), no se permite
                // anular, porque dejaria el stock en un valor negativo/incorrecto.
                if (nuevoStock < 0) {
                    throw new Exception("No se puede anular: el producto '" + producto.getNombre()
                            + "' ya tiene un stock actual menor al que aporto esta orden.");
                }
                producto.setStockActual(nuevoStock);
                repositorioProducto.save(producto);
            }
        }

        orden.setEstado(EstadoOrden.ANULADA);
        orden.setFechaAnulacion(LocalDateTime.now());
        return repositorioOrdenCompra.save(orden);
    }

    @Override
    public List<OrdenCompra> findAll() throws Exception {
        return repositorioOrdenCompra.findAll();
    }

    @Override
    public OrdenCompra findById(long id) throws Exception {
        return repositorioOrdenCompra.findById(id)
                .orElseThrow(() -> new Exception("Orden de compra no encontrada con id: " + id));
    }

    @Override
    public OrdenCompra saveOne(OrdenCompra entity) throws Exception {
        // La creacion de ordenes siempre debe pasar por crearBorrador(), que arma
        // numero de orden, calcula subtotales/total y valida que tenga detalle.
        throw new UnsupportedOperationException("Usar crearBorrador() para crear ordenes de compra.");
    }

    @Override
    public OrdenCompra updateOne(OrdenCompra entity, long id) throws Exception {
        OrdenCompra existente = findById(id);
        if (existente.getEstado() != EstadoOrden.BORRADOR) {
            throw new Exception("Solo se puede editar una orden en estado BORRADOR.");
        }
        existente.setProveedor(entity.getProveedor());
        return repositorioOrdenCompra.save(existente);
    }

    @Override
    public boolean deleteById(long id) throws Exception {
        OrdenCompra orden = findById(id);
        // El borrado fisico solo aplica a BORRADOR, que nunca impacto stock ni tiene
        // valor historico. CONFIRMADA/ANULADA se conservan siempre como registro.
        if (orden.getEstado() != EstadoOrden.BORRADOR) {
            throw new Exception("Solo se puede eliminar fisicamente una orden en estado BORRADOR. Para las demas, usar anular().");
        }
        repositorioOrdenCompra.deleteById(id);
        return true;
    }
}
