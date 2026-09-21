package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.dtos.PanelDTO;
import com.colegio.sistemaescolar.entities.AsignacionDocente;
import com.colegio.sistemaescolar.repositories.RepositorioAlumno;
import com.colegio.sistemaescolar.repositories.RepositorioAula;
import com.colegio.sistemaescolar.repositories.RepositorioDocente;
import com.colegio.sistemaescolar.repositories.RepositorioGrado;
import com.colegio.sistemaescolar.repositories.RepositorioMateria;
import com.colegio.sistemaescolar.repositories.RepositorioNota;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Set;

/**
 * SERVICIO del tablero inicial (/panel): arma los contadores que muestra el dashboard.
 * El ADMIN ve totales del colegio; el DOCENTE ve la cantidad de alumnos de sus grados y sus asignaciones.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicioPanel {

    private final RepositorioAlumno repositorioAlumno;
    private final RepositorioDocente repositorioDocente;
    private final RepositorioGrado repositorioGrado;
    private final RepositorioAula repositorioAula;
    private final RepositorioMateria repositorioMateria;
    private final RepositorioNota repositorioNota;
    private final ServicioAcceso acceso;

    public PanelDTO obtener() {
        PanelDTO dto = new PanelDTO();
        dto.setAdmin(acceso.esAdmin());
        if (acceso.esAdmin()) {
            dto.setAlumnos(repositorioAlumno.countByEliminadoFalse());
            dto.setDocentes(repositorioDocente.countByEliminadoFalse());
            dto.setGrados(repositorioGrado.countByEliminadoFalse());
            dto.setAulas(repositorioAula.countByEliminadoFalse());
            dto.setMaterias(repositorioMateria.countByEliminadoFalse());
            dto.setNotas(repositorioNota.countByEliminadoFalse());
            dto.setPromedioGeneral(repositorioNota.promedioGeneral());
        } else {
            Set<Long> gradoIds = acceso.idsGradosPermitidos();
            dto.setAlumnos(gradoIds.isEmpty() ? 0 : repositorioAlumno.countByGradoIdInAndEliminadoFalse(gradoIds));
            var asignaciones = acceso.asignacionesActuales();
            asignaciones.stream()
                    .sorted(Comparator.comparingInt((AsignacionDocente x) -> x.getGrado().getNivel())
                            .thenComparing(x -> x.getMateria().getNombre()))
                    .forEach(x -> dto.getMisAsignaciones().add(x.getGrado().getNombre() + " - " + x.getMateria().getNombre()));
            dto.setGrados(gradoIds.size());
            dto.setMaterias(asignaciones.stream().map(x -> x.getMateria().getId()).distinct().count());
        }
        return dto;
    }
}
