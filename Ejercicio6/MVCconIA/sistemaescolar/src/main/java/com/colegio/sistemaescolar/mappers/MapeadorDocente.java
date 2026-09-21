package com.colegio.sistemaescolar.mappers;

import com.colegio.sistemaescolar.dtos.DocenteDTO;
import com.colegio.sistemaescolar.entities.AsignacionDocente;
import com.colegio.sistemaescolar.entities.Docente;
import com.colegio.sistemaescolar.utils.Textos;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MAPPER de Docente: convierte entidad ↔ {@link DocenteDTO}.
 *
 * <p>Las asignaciones (docente + grado + materia) viven en otra entidad, por eso {@link #aDto} las recibe aparte.
 * Debe invocarse dentro de la transacción del servicio (el usuario del docente se carga de forma perezosa).</p>
 */
@Component
public class MapeadorDocente {

    /** Convierte un docente y sus asignaciones al DTO que ve la capa web. */
    public DocenteDTO aDto(Docente docente, List<AsignacionDocente> asignaciones) {
        DocenteDTO dto = new DocenteDTO();
        dto.setId(docente.getId());
        dto.setNombre(docente.getNombre());
        dto.setApellido(docente.getApellido());
        dto.setSexo(docente.getSexo());
        dto.setFechaNacimiento(docente.getFechaNacimiento());
        dto.setEmail(docente.getUsuario().getEmail());
        dto.setHabilitado(docente.getUsuario().isHabilitado());

        // "gradoId-materiaId": misma clave que usan los checkboxes del formulario.
        dto.setAsignaciones(asignaciones.stream()
                .map(a -> clave(a.getGrado().getId(), a.getMateria().getId()))
                .collect(Collectors.toSet()));
        dto.setAsignacionesTexto(asignaciones.stream()
                .sorted(Comparator.comparingInt((AsignacionDocente a) -> a.getGrado().getNivel())
                        .thenComparing(a -> a.getMateria().getNombre()))
                .map(a -> a.getGrado().getNombre() + " - " + a.getMateria().getNombre())
                .collect(Collectors.toList()));
        return dto;
    }

    /** Copia al docente solo los datos personales; el correo y las asignaciones se manejan aparte. */
    public void aplicarDatosPersonales(DocenteDTO dto, Docente docente) {
        docente.setNombre(Textos.limpiarNoNulo(dto.getNombre()));
        docente.setApellido(Textos.limpiarNoNulo(dto.getApellido()));
        docente.setSexo(dto.getSexo());
        docente.setFechaNacimiento(dto.getFechaNacimiento());
    }

    /** Clave de una asignación tal como viaja en el formulario. */
    public static String clave(long gradoId, long materiaId) {
        return gradoId + "-" + materiaId;
    }
}
