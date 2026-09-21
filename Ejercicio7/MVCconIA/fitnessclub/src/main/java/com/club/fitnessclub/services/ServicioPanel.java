package com.club.fitnessclub.services;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.club.fitnessclub.dtos.ResumenPanelDTO;
import com.club.fitnessclub.enums.EstadoCuota;
import com.club.fitnessclub.repositories.RepositorioCuota;
import com.club.fitnessclub.repositories.RepositorioGrupoFamiliar;
import com.club.fitnessclub.repositories.RepositorioPago;
import com.club.fitnessclub.repositories.RepositorioPersona;
import com.club.fitnessclub.repositories.RepositorioRegistroAcceso;

import lombok.RequiredArgsConstructor;

/** Servicio de solo lectura que arma los contadores del tablero (dashboard) inicial. */
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ServicioPanel {

    private final RepositorioGrupoFamiliar repositorioGrupo;
    private final RepositorioPersona repositorioPersona;
    private final RepositorioRegistroAcceso repositorioAcceso;
    private final RepositorioCuota repositorioCuota;
    private final RepositorioPago repositorioPago;
    private final Clock reloj;

    public ResumenPanelDTO obtenerResumen() {
        LocalDate hoy = LocalDate.now(reloj);
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime inicioMes = YearMonth.from(hoy).atDay(1).atStartOfDay();

        ResumenPanelDTO resumen = new ResumenPanelDTO();
        resumen.setFamiliasActivas(repositorioGrupo.countByActivoTrue());
        resumen.setPersonasActivas(repositorioPersona.countByActivoTrue());
        resumen.setDentroAhora(repositorioAcceso.countByFechaHoraSalidaIsNull());
        resumen.setAccesosHoy(repositorioAcceso.countByFechaHoraEntradaBetween(inicioDia, inicioDia.plusDays(1)));
        resumen.setCuotasPendientes(repositorioCuota.countByEstadoAndActivoTrue(EstadoCuota.PENDIENTE));
        resumen.setCuotasVencidas(
                repositorioCuota.countByEstadoAndFechaVencimientoBeforeAndActivoTrue(EstadoCuota.PENDIENTE, hoy));

        BigDecimal recaudado = repositorioPago.sumarMontoEntre(inicioMes, inicioMes.plusMonths(1));
        resumen.setRecaudadoMes(recaudado == null ? BigDecimal.ZERO : recaudado);
        return resumen;
    }
}
