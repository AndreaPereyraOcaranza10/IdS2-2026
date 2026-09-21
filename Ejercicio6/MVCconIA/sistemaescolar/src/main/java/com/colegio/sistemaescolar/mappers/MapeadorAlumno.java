package com.colegio.sistemaescolar.mappers;

import com.colegio.sistemaescolar.dtos.AlumnoDTO;
import com.colegio.sistemaescolar.entities.Alumno;
import com.colegio.sistemaescolar.utils.Textos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

/**
 * MAPEADOR Entidad <-> DTO de Alumno.
 * Debe invocarse DENTRO de la transacción del servicio: lee {@code grado} y {@code aula}, que son LAZY.
 * La edad se calcula con el {@code Clock} inyectado (más fácil de probar que {@code LocalDate.now()}).
 */
@Component
@RequiredArgsConstructor
public class MapeadorAlumno {

    private final Clock reloj;

    public AlumnoDTO aDto(Alumno alumno) {
        AlumnoDTO dto = new AlumnoDTO();
        dto.setId(alumno.getId());
        dto.setDni(alumno.getDni());
        dto.setNombre(alumno.getNombre());
        dto.setApellido(alumno.getApellido());
        dto.setSexo(alumno.getSexo());
        dto.setFechaNacimiento(alumno.getFechaNacimiento());
        dto.setDomicilio(alumno.getDomicilio());
        dto.setNombreTutor(alumno.getNombreTutor());
        dto.setTelefonoTutor(alumno.getTelefonoTutor());
        dto.setGradoId(alumno.getGrado().getId());
        dto.setAulaId(alumno.getAula().getId());
        dto.setGradoNombre(alumno.getGrado().getNombre());
        dto.setAulaCodigo(alumno.getAula().getCodigo());
        dto.setEdad(Period.between(alumno.getFechaNacimiento(), LocalDate.now(reloj)).getYears());
        return dto;
    }

    /** Copia los datos escalares; el grado y el aula los asigna el servicio (después de validarlos). */
    public void aplicar(AlumnoDTO dto, Alumno alumno) {
        alumno.setDni(Textos.limpiarNoNulo(dto.getDni()));
        alumno.setNombre(Textos.limpiarNoNulo(dto.getNombre()));
        alumno.setApellido(Textos.limpiarNoNulo(dto.getApellido()));
        alumno.setSexo(dto.getSexo());
        alumno.setFechaNacimiento(dto.getFechaNacimiento());
        alumno.setDomicilio(Textos.limpiar(dto.getDomicilio()));
        alumno.setNombreTutor(Textos.limpiar(dto.getNombreTutor()));
        alumno.setTelefonoTutor(Textos.limpiar(dto.getTelefonoTutor()));
    }
}
