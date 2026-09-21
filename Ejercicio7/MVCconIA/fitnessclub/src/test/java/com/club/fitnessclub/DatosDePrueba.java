package com.club.fitnessclub;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.club.fitnessclub.entities.Cuota;
import com.club.fitnessclub.entities.GrupoFamiliar;
import com.club.fitnessclub.entities.Pago;
import com.club.fitnessclub.entities.Persona;
import com.club.fitnessclub.enums.EstadoCuota;
import com.club.fitnessclub.enums.MedioPago;
import com.club.fitnessclub.enums.Parentesco;

/**
 * Fábrica de objetos de prueba (test data builders) compartida por las pruebas unitarias.
 * Centraliza la creación de entidades para que cada test se enfoque en lo que verifica.
 */
public final class DatosDePrueba {

    /** Reloj fijo: "hoy" es siempre 2026-09-15 12:00 (UTC). Vuelve deterministas los tests con fechas. */
    public static final Clock RELOJ = Clock.fixed(Instant.parse("2026-09-15T12:00:00Z"), ZoneId.of("UTC"));
    public static final LocalDate HOY = LocalDate.of(2026, 9, 15);

    private DatosDePrueba() { }

    public static GrupoFamiliar grupo(long id, String nombre) {
        GrupoFamiliar g = new GrupoFamiliar();
        g.setId(id);
        g.setNombre(nombre);
        return g;
    }

    /** Persona vigente asociada al grupo (sincroniza ambos lados de la relación). */
    public static Persona persona(long id, String dni, GrupoFamiliar grupo, Parentesco parentesco) {
        Persona p = new Persona();
        p.setId(id);
        p.setNombre("Nombre" + id);
        p.setApellido("Apellido" + id);
        p.setDni(dni);
        p.setParentesco(parentesco);
        grupo.agregarIntegrante(p);
        return p;
    }

    /** Cuota PENDIENTE del período dado (primer día del mes) con vencimiento el día 10. */
    public static Cuota cuota(long id, GrupoFamiliar grupo, String importe, LocalDate periodo) {
        Cuota c = new Cuota();
        c.setId(id);
        c.setGrupoFamiliar(grupo);
        c.setPeriodo(periodo);
        c.setImporte(new BigDecimal(importe));
        c.setFechaVencimiento(periodo.plusDays(9));
        c.setEstado(EstadoCuota.PENDIENTE);
        return c;
    }

    /** Pago vigente ya asociado a la cuota. */
    public static Pago pago(long id, Cuota cuota, String monto, MedioPago medio) {
        Pago p = new Pago();
        p.setId(id);
        p.setMonto(new BigDecimal(monto));
        p.setMedioPago(medio);
        p.setFechaPago(HOY.atStartOfDay());
        cuota.agregarPago(p);
        return p;
    }
}
