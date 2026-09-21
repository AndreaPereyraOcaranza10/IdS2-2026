package com.club.fitnessclub.mappers;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.club.fitnessclub.dtos.RegistroAccesoDTO;
import com.club.fitnessclub.entities.Persona;
import com.club.fitnessclub.entities.RegistroAcceso;

import lombok.RequiredArgsConstructor;

/**
 * Mapeador RegistroAcceso (entidad) -> RegistroAccesoDTO. Solo lectura: los accesos no se crean
 * desde formularios sino desde ServicioAcceso (entrada/salida por DNI).
 *
 * <p>Usa el Clock inyectado para calcular la permanencia de las visitas abiertas.
 * Requiere persona y persona.grupoFamiliar cargados (los repositorios usan @EntityGraph).
 */
@Component
@RequiredArgsConstructor
public class MapeadorRegistroAcceso {

    private final Clock reloj;

    public RegistroAccesoDTO aDTO(RegistroAcceso acceso) {
        Persona persona = acceso.getPersona();

        RegistroAccesoDTO dto = new RegistroAccesoDTO();
        dto.setId(acceso.getId());
        dto.setPersonaId(persona.getId());
        dto.setPersonaNombre(persona.getNombreCompleto());
        dto.setPersonaDni(persona.getDni());
        dto.setGrupoFamiliarNombre(persona.getGrupoFamiliar().getNombre());
        dto.setTieneRostro(persona.getImagenRostro() != null);
        dto.setFechaHoraEntrada(acceso.getFechaHoraEntrada());
        dto.setFechaHoraSalida(acceso.getFechaHoraSalida());
        dto.setAdentro(acceso.estaAdentro());

        LocalDateTime hasta = acceso.estaAdentro() ? LocalDateTime.now(reloj) : acceso.getFechaHoraSalida();
        dto.setPermanencia(formatearDuracion(Duration.between(acceso.getFechaHoraEntrada(), hasta)));
        return dto;
    }

    /** 80 minutos -> "1 h 20 min"; 45 minutos -> "45 min". Nunca devuelve valores negativos. */
    static String formatearDuracion(Duration duracion) {
        long minutos = Math.max(0, duracion.toMinutes());
        long horas = minutos / 60;
        long resto = minutos % 60;
        return horas > 0 ? horas + " h " + resto + " min" : resto + " min";
    }
}
