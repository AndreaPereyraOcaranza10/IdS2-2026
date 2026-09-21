package com.club.fitnessclub.dtos;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Contadores del tablero (dashboard) de la pantalla inicial del panel. */
@Data
@NoArgsConstructor
public class ResumenPanelDTO {

    private long familiasActivas;
    private long personasActivas;
    /** Personas con una visita abierta (entrada sin salida). */
    private long dentroAhora;
    private long accesosHoy;
    private long cuotasPendientes;
    private long cuotasVencidas;
    /** Total cobrado en el mes en curso (solo se muestra a ADMIN). */
    private BigDecimal recaudadoMes = BigDecimal.ZERO;
}
