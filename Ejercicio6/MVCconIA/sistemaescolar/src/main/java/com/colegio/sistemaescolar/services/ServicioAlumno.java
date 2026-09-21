package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.dtos.AlumnoDTO;
import com.colegio.sistemaescolar.entities.Alumno;
import com.colegio.sistemaescolar.entities.Aula;
import com.colegio.sistemaescolar.entities.Grado;
import com.colegio.sistemaescolar.entities.Nota;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.mappers.MapeadorAlumno;
import com.colegio.sistemaescolar.repositories.RepositorioAlumno;
import com.colegio.sistemaescolar.repositories.RepositorioAula;
import com.colegio.sistemaescolar.repositories.RepositorioGrado;
import com.colegio.sistemaescolar.repositories.RepositorioNota;
import com.colegio.sistemaescolar.utils.Textos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * SERVICIO de Alumnos.
 *
 * <h2>Reglas de negocio</h2>
 * <ul>
 *   <li>El DNI es único.</li>
 *   <li>Todo alumno tiene un grado y un aula, y el aula no puede superar su capacidad.</li>
 *   <li>Solo el ADMIN da de alta, modifica o da de baja alumnos.</li>
 *   <li>Un DOCENTE solo ve (lista y detalle) a los alumnos de los grados que tiene asignados.</li>
 *   <li>Al dar de baja un alumno también se dan de baja (lógica) sus notas.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ServicioAlumno implements ServicioBase<AlumnoDTO> {

    private final RepositorioAlumno repositorio;
    private final RepositorioGrado repositorioGrado;
    private final RepositorioAula repositorioAula;
    private final RepositorioNota repositorioNota;
    private final MapeadorAlumno mapeador;
    private final ServicioAcceso acceso;

    @Override
    @Transactional(readOnly = true)
    public List<AlumnoDTO> listar() {
        List<Alumno> alumnos;
        if (acceso.esAdmin()) {
            alumnos = repositorio.findByEliminadoFalseOrderByApellidoAscNombreAsc();
        } else {
            Set<Long> gradosPermitidos = acceso.idsGradosPermitidos();
            if (gradosPermitidos.isEmpty()) {
                alumnos = new ArrayList<>();
            } else {
                alumnos = repositorio.findByGradoIdInAndEliminadoFalseOrderByApellidoAscNombreAsc(gradosPermitidos);
            }
        }
        List<AlumnoDTO> resultado = new ArrayList<>();
        for (Alumno alumno : alumnos) {
            resultado.add(mapeador.aDto(alumno));
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public AlumnoDTO buscarPorId(long id) throws ExcepcionNegocio {
        Alumno alumno = obtener(id);
        verificarAcceso(alumno);
        return mapeador.aDto(alumno);
    }

    @Override
    public AlumnoDTO crear(AlumnoDTO dto) throws ExcepcionNegocio {
        exigirAdmin();
        String dni = Textos.limpiarNoNulo(dto.getDni());
        if (repositorio.existsByDni(dni)) {
            throw new ExcepcionNegocio("dni", "Ya existe un alumno con ese DNI.");
        }
        Grado grado = obtenerGrado(dto.getGradoId());
        Aula aula = obtenerAula(dto.getAulaId());
        validarCupo(aula);

        Alumno alumno = new Alumno();
        mapeador.aplicar(dto, alumno);
        alumno.setGrado(grado);
        alumno.setAula(aula);
        return mapeador.aDto(repositorio.save(alumno));
    }

    @Override
    public AlumnoDTO actualizar(long id, AlumnoDTO dto) throws ExcepcionNegocio {
        exigirAdmin();
        Alumno alumno = obtener(id);
        String dni = Textos.limpiarNoNulo(dto.getDni());
        if (repositorio.existsByDniAndIdNot(dni, id)) {
            throw new ExcepcionNegocio("dni", "Ya existe otro alumno con ese DNI.");
        }
        Grado grado = obtenerGrado(dto.getGradoId());
        Aula aula = obtenerAula(dto.getAulaId());
        // Solo se controla el cupo si el alumno cambia de aula (si sigue en la misma ya cuenta como ocupante).
        if (alumno.getAula().getId() != aula.getId()) {
            validarCupo(aula);
        }

        mapeador.aplicar(dto, alumno);
        alumno.setGrado(grado);
        alumno.setAula(aula);
        return mapeador.aDto(repositorio.save(alumno));
    }

    @Override
    public void eliminar(long id) throws ExcepcionNegocio {
        exigirAdmin();
        Alumno alumno = obtener(id);
        alumno.setEliminado(true);
        repositorio.save(alumno);
        // Baja lógica en cascada de las notas del alumno (queda registrado quién la hizo).
        for (Nota nota : repositorioNota.findByAlumnoIdAndEliminadoFalseOrderByFechaAscIdAsc(id)) {
            nota.setEliminado(true);
            repositorioNota.save(nota);
        }
    }

    // ------------------------------------------------------------------ auxiliares

    private Alumno obtener(long id) throws ExcepcionNegocio {
        return repositorio.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ExcepcionNegocio("El alumno solicitado no existe."));
    }

    private Grado obtenerGrado(Long id) throws ExcepcionNegocio {
        if (id == null) {
            throw new ExcepcionNegocio("gradoId", "Seleccione el grado.");
        }
        return repositorioGrado.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ExcepcionNegocio("gradoId", "El grado seleccionado no existe."));
    }

    private Aula obtenerAula(Long id) throws ExcepcionNegocio {
        if (id == null) {
            throw new ExcepcionNegocio("aulaId", "Seleccione el aula.");
        }
        return repositorioAula.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ExcepcionNegocio("aulaId", "El aula seleccionada no existe."));
    }

    private void validarCupo(Aula aula) throws ExcepcionNegocio {
        long ocupados = repositorio.countByAulaIdAndEliminadoFalse(aula.getId());
        if (ocupados >= aula.getCapacidad()) {
            throw new ExcepcionNegocio("aulaId", "El aula " + aula.getCodigo() + " no tiene cupo disponible ("
                    + ocupados + " de " + aula.getCapacidad() + ").");
        }
    }

    private void exigirAdmin() throws ExcepcionNegocio {
        if (!acceso.esAdmin()) {
            throw new ExcepcionNegocio("Solo el personal administrador puede modificar alumnos.");
        }
    }

    /** Un docente solo puede acceder a alumnos de sus grados. */
    private void verificarAcceso(Alumno alumno) throws ExcepcionNegocio {
        if (acceso.esAdmin()) {
            return;
        }
        if (!acceso.idsGradosPermitidos().contains(alumno.getGrado().getId())) {
            throw new ExcepcionNegocio("No tiene acceso a ese alumno: no pertenece a ninguno de sus grados.");
        }
    }
}
