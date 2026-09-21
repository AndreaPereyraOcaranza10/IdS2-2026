package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.dtos.AlumnoDTO;
import com.colegio.sistemaescolar.dtos.BoletinDTO;
import com.colegio.sistemaescolar.dtos.BoletinMateriaDTO;
import com.colegio.sistemaescolar.dtos.MateriaDTO;
import com.colegio.sistemaescolar.dtos.NotaDTO;
import com.colegio.sistemaescolar.entities.Alumno;
import com.colegio.sistemaescolar.entities.Materia;
import com.colegio.sistemaescolar.entities.Nota;
import com.colegio.sistemaescolar.enums.Periodo;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.mappers.MapeadorAlumno;
import com.colegio.sistemaescolar.mappers.MapeadorMateria;
import com.colegio.sistemaescolar.mappers.MapeadorNota;
import com.colegio.sistemaescolar.repositories.RepositorioAlumno;
import com.colegio.sistemaescolar.repositories.RepositorioMateria;
import com.colegio.sistemaescolar.repositories.RepositorioNota;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * SERVICIO de Notas por materia.
 *
 * <h2>Reglas de negocio</h2>
 * <ul>
 *   <li>Escala de 1 a 10 con dos decimales; se aprueba con 6 o más.</li>
 *   <li><b>Autorización por datos</b>: un DOCENTE solo puede ver, cargar, modificar o eliminar notas de
 *       alumnos de SUS grados en SUS materias. El ADMIN puede todo. (Seguridad en dos niveles: el rol
 *       habilita las pantallas y este servicio limita los datos.)</li>
 *   <li>Quién cargó cada nota queda en los campos de auditoría.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ServicioNota implements ServicioBase<NotaDTO> {

    private final RepositorioNota repositorio;
    private final RepositorioAlumno repositorioAlumno;
    private final RepositorioMateria repositorioMateria;
    private final MapeadorNota mapeador;
    private final MapeadorAlumno mapeadorAlumno;
    private final MapeadorMateria mapeadorMateria;
    private final ServicioAcceso acceso;

    // ------------------------------------------------------------------ CRUD

    @Override
    @Transactional(readOnly = true)
    public List<NotaDTO> listar() {
        List<Nota> notas;
        if (acceso.esAdmin()) {
            notas = repositorio.findByEliminadoFalseOrderByFechaDescIdDesc();
        } else {
            // La consulta ya aplica la regla "grado del alumno + materia" contra las asignaciones del docente.
            notas = acceso.docenteActual()
                    .map(docente -> repositorio.buscarVisibles(docente.getId()))
                    .orElseGet(ArrayList::new);
        }
        List<NotaDTO> resultado = new ArrayList<>();
        for (Nota nota : notas) {
            resultado.add(mapeador.aDto(nota));
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public NotaDTO buscarPorId(long id) throws ExcepcionNegocio {
        Nota nota = obtener(id);
        verificarPermiso(nota.getAlumno(), nota.getMateria());
        return mapeador.aDto(nota);
    }

    @Override
    public NotaDTO crear(NotaDTO dto) throws ExcepcionNegocio {
        Alumno alumno = obtenerAlumno(dto.getAlumnoId());
        Materia materia = obtenerMateria(dto.getMateriaId());
        verificarPermiso(alumno, materia);

        Nota nota = new Nota();
        mapeador.aplicar(dto, nota);
        nota.setAlumno(alumno);
        nota.setMateria(materia);
        return mapeador.aDto(repositorio.save(nota));
    }

    @Override
    public NotaDTO actualizar(long id, NotaDTO dto) throws ExcepcionNegocio {
        Nota nota = obtener(id);
        // Debe estar autorizado sobre la nota actual Y sobre el alumno/materia a los que la quiere pasar.
        verificarPermiso(nota.getAlumno(), nota.getMateria());
        Alumno alumno = obtenerAlumno(dto.getAlumnoId());
        Materia materia = obtenerMateria(dto.getMateriaId());
        verificarPermiso(alumno, materia);

        mapeador.aplicar(dto, nota);
        nota.setAlumno(alumno);
        nota.setMateria(materia);
        return mapeador.aDto(repositorio.save(nota));
    }

    @Override
    public void eliminar(long id) throws ExcepcionNegocio {
        Nota nota = obtener(id);
        verificarPermiso(nota.getAlumno(), nota.getMateria());
        nota.setEliminado(true);
        repositorio.save(nota);
    }

    // ------------------------------------------------------------------ opciones del formulario

    /** Alumnos que el usuario actual puede calificar (todos para ADMIN; los de sus grados para DOCENTE). */
    @Transactional(readOnly = true)
    public List<AlumnoDTO> alumnosDisponibles() {
        List<Alumno> alumnos;
        if (acceso.esAdmin()) {
            alumnos = repositorioAlumno.findByEliminadoFalseOrderByApellidoAscNombreAsc();
        } else {
            Set<Long> grados = acceso.idsGradosPermitidos();
            if (grados.isEmpty()) {
                alumnos = new ArrayList<>();
            } else {
                alumnos = repositorioAlumno.findByGradoIdInAndEliminadoFalseOrderByApellidoAscNombreAsc(grados);
            }
        }
        List<AlumnoDTO> resultado = new ArrayList<>();
        for (Alumno alumno : alumnos) {
            resultado.add(mapeadorAlumno.aDto(alumno));
        }
        return resultado;
    }

    /** Materias que el usuario actual puede calificar (todas para ADMIN; las que dicta para DOCENTE). */
    @Transactional(readOnly = true)
    public List<MateriaDTO> materiasDisponibles() {
        List<Materia> materias;
        if (acceso.esAdmin()) {
            materias = repositorioMateria.findByEliminadoFalseOrderByNombreAsc();
        } else {
            Set<Long> ids = acceso.idsMateriasPermitidas();
            if (ids.isEmpty()) {
                materias = new ArrayList<>();
            } else {
                materias = repositorioMateria.findByIdInAndEliminadoFalseOrderByNombreAsc(ids);
            }
        }
        List<MateriaDTO> resultado = new ArrayList<>();
        for (Materia materia : materias) {
            resultado.add(mapeadorMateria.aDto(materia));
        }
        return resultado;
    }

    // ------------------------------------------------------------------ boletín

    /**
     * Arma el boletín de un alumno: por cada materia, el promedio de cada período y el promedio final.
     * Un docente ve solamente las materias que dicta.
     */
    @Transactional(readOnly = true)
    public BoletinDTO boletin(long alumnoId) throws ExcepcionNegocio {
        Alumno alumno = repositorioAlumno.findByIdAndEliminadoFalse(alumnoId)
                .orElseThrow(() -> new ExcepcionNegocio("El alumno solicitado no existe."));
        if (!acceso.esAdmin() && !acceso.idsGradosPermitidos().contains(alumno.getGrado().getId())) {
            throw new ExcepcionNegocio("No tiene acceso a ese alumno: no pertenece a ninguno de sus grados.");
        }

        List<Nota> notas = repositorio.findByAlumnoIdAndEliminadoFalseOrderByFechaAscIdAsc(alumnoId);
        if (!acceso.esAdmin()) {
            long gradoId = alumno.getGrado().getId();
            notas = notas.stream()
                    .filter(n -> acceso.puedeCalificar(gradoId, n.getMateria().getId()))
                    .collect(Collectors.toList());
        }

        // Agrupa por nombre de materia (TreeMap = orden alfabético).
        Map<String, List<Nota>> porMateria = notas.stream()
                .collect(Collectors.groupingBy(n -> n.getMateria().getNombre(), TreeMap::new, Collectors.toList()));

        BoletinDTO boletin = new BoletinDTO();
        boletin.setAlumnoId(alumno.getId());
        boletin.setAlumnoNombre(alumno.getApellido() + ", " + alumno.getNombre());
        boletin.setGradoNombre(alumno.getGrado().getNombre());
        boletin.setAulaCodigo(alumno.getAula().getCodigo());

        List<BigDecimal> promediosMaterias = new ArrayList<>();
        for (Map.Entry<String, List<Nota>> entrada : porMateria.entrySet()) {
            BoletinMateriaDTO fila = new BoletinMateriaDTO();
            fila.setMateria(entrada.getKey());

            List<BigDecimal> promediosPeriodos = new ArrayList<>();
            for (Periodo periodo : Periodo.values()) {
                List<BigDecimal> valores = new ArrayList<>();
                for (Nota nota : entrada.getValue()) {
                    if (nota.getPeriodo() == periodo) {
                        valores.add(nota.getValor());
                    }
                }
                if (!valores.isEmpty()) {
                    BigDecimal promedioPeriodo = promediar(valores);
                    fila.getPromedios().put(periodo, promedioPeriodo);
                    promediosPeriodos.add(promedioPeriodo);
                }
            }
            BigDecimal promedioMateria = promediar(promediosPeriodos);
            fila.setPromedio(promedioMateria);
            fila.setAprobada(promedioMateria.compareTo(MapeadorNota.NOTA_APROBACION) >= 0);
            boletin.getMaterias().add(fila);
            promediosMaterias.add(promedioMateria);
        }
        if (!promediosMaterias.isEmpty()) {
            boletin.setPromedioGeneral(promediar(promediosMaterias));
        }
        return boletin;
    }

    // ------------------------------------------------------------------ auxiliares

    private Nota obtener(long id) throws ExcepcionNegocio {
        return repositorio.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ExcepcionNegocio("La nota solicitada no existe."));
    }

    private Alumno obtenerAlumno(Long id) throws ExcepcionNegocio {
        if (id == null) {
            throw new ExcepcionNegocio("alumnoId", "Seleccione el alumno.");
        }
        return repositorioAlumno.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ExcepcionNegocio("alumnoId", "El alumno seleccionado no existe."));
    }

    private Materia obtenerMateria(Long id) throws ExcepcionNegocio {
        if (id == null) {
            throw new ExcepcionNegocio("materiaId", "Seleccione la materia.");
        }
        return repositorioMateria.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ExcepcionNegocio("materiaId", "La materia seleccionada no existe."));
    }

    /** Autorización a nivel de datos: el docente debe dictar ESA materia en el grado del alumno (asignación exacta). */
    private void verificarPermiso(Alumno alumno, Materia materia) throws ExcepcionNegocio {
        if (!acceso.puedeCalificar(alumno.getGrado().getId(), materia.getId())) {
            throw new ExcepcionNegocio("No está autorizado/a para calificar a ese alumno en esa materia: "
                    + "solo puede hacerlo en las materias que dicta en el grado del alumno.");
        }
    }

    /** Promedio aritmético con dos decimales (redondeo "half up"). */
    private BigDecimal promediar(List<BigDecimal> valores) {
        BigDecimal suma = BigDecimal.ZERO;
        for (BigDecimal valor : valores) {
            suma = suma.add(valor);
        }
        return suma.divide(BigDecimal.valueOf(valores.size()), 2, RoundingMode.HALF_UP);
    }
}
