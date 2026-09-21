package com.club.fitnessclub.services;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.club.fitnessclub.dtos.RegistroAccesoDTO;
import com.club.fitnessclub.entities.Persona;
import com.club.fitnessclub.entities.RegistroAcceso;
import com.club.fitnessclub.enums.EstadoCuota;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.mappers.MapeadorRegistroAcceso;
import com.club.fitnessclub.repositories.RepositorioCuota;
import com.club.fitnessclub.repositories.RepositorioPersona;
import com.club.fitnessclub.repositories.RepositorioRegistroAcceso;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de ACCESOS: registra el horario de entrada y de salida de cada persona del club.
 *
 * <p>No implementa ServicioBase: los accesos no tienen ABM clásico (no se editan ni se borran),
 * solo se registran entradas y salidas identificando a la persona por DNI.
 *
 * <p>REGLAS DE NEGOCIO:
 * <ol>
 *   <li>Solo pueden ingresar personas vigentes (no dadas de baja).</li>
 *   <li>No se puede registrar una entrada si la persona ya tiene una visita abierta.</li>
 *   <li>No se puede registrar una salida sin una entrada abierta.</li>
 *   <li>Si la familia tiene cuotas vencidas sin pagar, la entrada se registra igual pero con una
 *       ADVERTENCIA para recepción (no bloquea el ingreso).</li>
 * </ol>
 * La hora sale del Clock inyectado (no del cliente): el usuario no puede falsear el horario.
 *
 * <p>LIMITACIÓN CONOCIDA: dos entradas simultáneas del MISMO DNI en el mismo instante podrían crear
 * dos visitas abiertas (no hay restricción única en la BD). Es un caso extremadamente raro en
 * recepción y está documentado en el plan de pruebas de estrés.
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ServicioAcceso {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final RepositorioRegistroAcceso repositorioAcceso;
    private final RepositorioPersona repositorioPersona;
    private final RepositorioCuota repositorioCuota;
    private final MapeadorRegistroAcceso mapeador;
    private final Clock reloj;

    /** Registra la ENTRADA de la persona identificada por DNI. */
    public RegistroAccesoDTO registrarEntrada(String dni) throws Exception {
        Persona persona = buscarPersonaPorDni(dni);

        // Regla 2: no puede tener otra visita abierta.
        RegistroAcceso abierto = repositorioAcceso.findFirstByPersonaIdAndFechaHoraSalidaIsNull(persona.getId())
                .orElse(null);
        if (abierto != null) {
            throw new ExcepcionNegocio(persona.getNombreCompleto() + " ya tiene una entrada registrada a las "
                    + HORA.format(abierto.getFechaHoraEntrada()) + ". Registre primero su salida.");
        }

        RegistroAcceso acceso = new RegistroAcceso();
        acceso.setPersona(persona);
        acceso.setFechaHoraEntrada(LocalDateTime.now(reloj));
        RegistroAccesoDTO dto = mapeador.aDTO(repositorioAcceso.save(acceso));

        // Regla 4: advertencia (no bloqueante) si la familia tiene cuotas vencidas.
        boolean deuda = repositorioCuota.existsByGrupoFamiliarIdAndEstadoAndFechaVencimientoBeforeAndActivoTrue(
                persona.getGrupoFamiliar().getId(), EstadoCuota.PENDIENTE, LocalDate.now(reloj));
        if (deuda) {
            dto.setAdvertencia("La familia " + persona.getGrupoFamiliar().getNombre()
                    + " tiene cuotas vencidas sin pagar.");
        }
        return dto;
    }

    /** Registra la SALIDA de la persona identificada por DNI (cierra su visita abierta). */
    public RegistroAccesoDTO registrarSalida(String dni) throws Exception {
        Persona persona = buscarPersonaPorDni(dni);

        // Regla 3: debe existir una entrada abierta.
        RegistroAcceso acceso = repositorioAcceso.findFirstByPersonaIdAndFechaHoraSalidaIsNull(persona.getId())
                .orElseThrow(() -> new ExcepcionNegocio(persona.getNombreCompleto()
                        + " no tiene una entrada abierta: no se puede registrar la salida."));

        acceso.setFechaHoraSalida(LocalDateTime.now(reloj));
        return mapeador.aDTO(repositorioAcceso.save(acceso));
    }

    /** Personas que están dentro del club ahora mismo. */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<RegistroAccesoDTO> findAdentro() throws Exception {
        return repositorioAcceso.findByFechaHoraSalidaIsNullOrderByFechaHoraEntradaDesc().stream()
                .map(mapeador::aDTO)
                .toList();
    }

    /** Últimos 100 movimientos (historial). */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<RegistroAccesoDTO> findUltimosMovimientos() throws Exception {
        return repositorioAcceso.findTop100ByOrderByFechaHoraEntradaDesc().stream()
                .map(mapeador::aDTO)
                .toList();
    }

    /** Valida el formato del DNI y busca a la persona vigente (regla 1). */
    private Persona buscarPersonaPorDni(String dni) throws ExcepcionNegocio {
        String limpio = dni == null ? "" : dni.trim();
        if (!limpio.matches("\\d{7,9}")) {
            throw new ExcepcionNegocio("Ingrese un DNI válido (7 a 9 dígitos, sin puntos).");
        }
        return repositorioPersona.findByDniAndActivoTrue(limpio)
                .orElseThrow(() -> new ExcepcionNegocio("No hay una persona vigente con DNI " + limpio + "."));
    }
}
