package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.dtos.GradoDTO;
import com.colegio.sistemaescolar.entities.Grado;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.mappers.MapeadorGrado;
import com.colegio.sistemaescolar.repositories.RepositorioAlumno;
import com.colegio.sistemaescolar.repositories.RepositorioDocente;
import com.colegio.sistemaescolar.repositories.RepositorioGrado;
import com.colegio.sistemaescolar.utils.Textos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * SERVICIO de Grados: reglas de negocio + transacciones.
 *
 * <ul>
 *   <li>{@code @Service}: bean de la capa de negocio.</li>
 *   <li>{@code @RequiredArgsConstructor} (Lombok): genera el constructor con los atributos {@code final};
 *       Spring inyecta las dependencias por constructor (mejor práctica frente a {@code @Autowired} en campos).</li>
 *   <li>{@code @Transactional(rollbackFor = Exception.class)}: cada método corre en una transacción que se
 *       revierte ante CUALQUIER excepción (incluida {@code ExcepcionNegocio}, que es verificada).
 *       Los métodos de consulta usan {@code readOnly = true} (más eficiente).</li>
 * </ul>
 *
 * <p>Reglas: el nombre del grado es único; no se puede dar de baja un grado con alumnos o docentes asignados.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ServicioGrado implements ServicioBase<GradoDTO> {

    private final RepositorioGrado repositorio;
    private final RepositorioAlumno repositorioAlumno;
    private final RepositorioDocente repositorioDocente;
    private final MapeadorGrado mapeador;

    @Override
    @Transactional(readOnly = true)
    public List<GradoDTO> listar() {
        List<GradoDTO> resultado = new ArrayList<>();
        for (Grado grado : repositorio.findByEliminadoFalseOrderByNivelAscNombreAsc()) {
            resultado.add(conConteo(grado));
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public GradoDTO buscarPorId(long id) throws ExcepcionNegocio {
        return conConteo(obtener(id));
    }

    @Override
    public GradoDTO crear(GradoDTO dto) throws ExcepcionNegocio {
        String nombre = Textos.limpiarNoNulo(dto.getNombre());
        if (repositorio.existsByNombreIgnoreCase(nombre)) {
            throw new ExcepcionNegocio("nombre", "Ya existe un grado con ese nombre.");
        }
        Grado grado = new Grado();
        mapeador.aplicar(dto, grado);
        return conConteo(repositorio.save(grado));
    }

    @Override
    public GradoDTO actualizar(long id, GradoDTO dto) throws ExcepcionNegocio {
        Grado grado = obtener(id);
        String nombre = Textos.limpiarNoNulo(dto.getNombre());
        if (repositorio.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ExcepcionNegocio("nombre", "Ya existe otro grado con ese nombre.");
        }
        mapeador.aplicar(dto, grado);
        return conConteo(repositorio.save(grado));
    }

    @Override
    public void eliminar(long id) throws ExcepcionNegocio {
        Grado grado = obtener(id);
        if (repositorioAlumno.existsByGradoIdAndEliminadoFalse(id)) {
            throw new ExcepcionNegocio("No se puede eliminar el grado \"" + grado.getNombre()
                    + "\": tiene alumnos asignados.");
        }
        if (repositorioDocente.existeDocenteConGrado(id)) {
            throw new ExcepcionNegocio("No se puede eliminar el grado \"" + grado.getNombre()
                    + "\": hay docentes asignados a él.");
        }
        grado.setEliminado(true);   // baja lógica; la auditoría registra quién y cuándo
        repositorio.save(grado);
    }

    private Grado obtener(long id) throws ExcepcionNegocio {
        return repositorio.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ExcepcionNegocio("El grado solicitado no existe."));
    }

    private GradoDTO conConteo(Grado grado) {
        GradoDTO dto = mapeador.aDto(grado);
        dto.setCantidadAlumnos(repositorioAlumno.countByGradoIdAndEliminadoFalse(grado.getId()));
        return dto;
    }
}
