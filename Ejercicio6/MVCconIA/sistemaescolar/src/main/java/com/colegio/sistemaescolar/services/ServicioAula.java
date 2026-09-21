package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.dtos.AulaDTO;
import com.colegio.sistemaescolar.entities.Aula;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.mappers.MapeadorAula;
import com.colegio.sistemaescolar.repositories.RepositorioAlumno;
import com.colegio.sistemaescolar.repositories.RepositorioAula;
import com.colegio.sistemaescolar.utils.Textos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * SERVICIO de Aulas (ver {@link ServicioGrado} para el detalle de las anotaciones).
 *
 * <p>Reglas: el código del aula es único; la capacidad no puede ser menor a los alumnos ya asignados;
 * no se puede dar de baja un aula con alumnos.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ServicioAula implements ServicioBase<AulaDTO> {

    private final RepositorioAula repositorio;
    private final RepositorioAlumno repositorioAlumno;
    private final MapeadorAula mapeador;

    @Override
    @Transactional(readOnly = true)
    public List<AulaDTO> listar() {
        List<AulaDTO> resultado = new ArrayList<>();
        for (Aula aula : repositorio.findByEliminadoFalseOrderByCodigoAsc()) {
            resultado.add(conOcupacion(aula));
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public AulaDTO buscarPorId(long id) throws ExcepcionNegocio {
        return conOcupacion(obtener(id));
    }

    @Override
    public AulaDTO crear(AulaDTO dto) throws ExcepcionNegocio {
        String codigo = Textos.limpiarNoNulo(dto.getCodigo());
        if (repositorio.existsByCodigoIgnoreCase(codigo)) {
            throw new ExcepcionNegocio("codigo", "Ya existe un aula con ese código.");
        }
        Aula aula = new Aula();
        mapeador.aplicar(dto, aula);
        return conOcupacion(repositorio.save(aula));
    }

    @Override
    public AulaDTO actualizar(long id, AulaDTO dto) throws ExcepcionNegocio {
        Aula aula = obtener(id);
        String codigo = Textos.limpiarNoNulo(dto.getCodigo());
        if (repositorio.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new ExcepcionNegocio("codigo", "Ya existe otra aula con ese código.");
        }
        long ocupados = repositorioAlumno.countByAulaIdAndEliminadoFalse(id);
        if (dto.getCapacidad() < ocupados) {
            throw new ExcepcionNegocio("capacidad", "La capacidad no puede ser menor a los alumnos ya asignados ("
                    + ocupados + ").");
        }
        mapeador.aplicar(dto, aula);
        return conOcupacion(repositorio.save(aula));
    }

    @Override
    public void eliminar(long id) throws ExcepcionNegocio {
        Aula aula = obtener(id);
        if (repositorioAlumno.existsByAulaIdAndEliminadoFalse(id)) {
            throw new ExcepcionNegocio("No se puede eliminar el aula \"" + aula.getCodigo()
                    + "\": tiene alumnos asignados.");
        }
        aula.setEliminado(true);
        repositorio.save(aula);
    }

    private Aula obtener(long id) throws ExcepcionNegocio {
        return repositorio.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ExcepcionNegocio("El aula solicitada no existe."));
    }

    private AulaDTO conOcupacion(Aula aula) {
        AulaDTO dto = mapeador.aDto(aula);
        dto.setOcupacion(repositorioAlumno.countByAulaIdAndEliminadoFalse(aula.getId()));
        return dto;
    }
}
