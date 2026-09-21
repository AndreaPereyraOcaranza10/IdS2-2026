package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.dtos.MateriaDTO;
import com.colegio.sistemaescolar.entities.Materia;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.mappers.MapeadorMateria;
import com.colegio.sistemaescolar.repositories.RepositorioDocente;
import com.colegio.sistemaescolar.repositories.RepositorioMateria;
import com.colegio.sistemaescolar.repositories.RepositorioNota;
import com.colegio.sistemaescolar.utils.Textos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * SERVICIO de Materias. Reglas: nombre único; no se puede dar de baja una materia con notas
 * cargadas o que dicte algún docente.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ServicioMateria implements ServicioBase<MateriaDTO> {

    private final RepositorioMateria repositorio;
    private final RepositorioNota repositorioNota;
    private final RepositorioDocente repositorioDocente;
    private final MapeadorMateria mapeador;

    @Override
    @Transactional(readOnly = true)
    public List<MateriaDTO> listar() {
        List<MateriaDTO> resultado = new ArrayList<>();
        for (Materia materia : repositorio.findByEliminadoFalseOrderByNombreAsc()) {
            resultado.add(mapeador.aDto(materia));
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public MateriaDTO buscarPorId(long id) throws ExcepcionNegocio {
        return mapeador.aDto(obtener(id));
    }

    @Override
    public MateriaDTO crear(MateriaDTO dto) throws ExcepcionNegocio {
        String nombre = Textos.limpiarNoNulo(dto.getNombre());
        if (repositorio.existsByNombreIgnoreCase(nombre)) {
            throw new ExcepcionNegocio("nombre", "Ya existe una materia con ese nombre.");
        }
        Materia materia = new Materia();
        mapeador.aplicar(dto, materia);
        return mapeador.aDto(repositorio.save(materia));
    }

    @Override
    public MateriaDTO actualizar(long id, MateriaDTO dto) throws ExcepcionNegocio {
        Materia materia = obtener(id);
        String nombre = Textos.limpiarNoNulo(dto.getNombre());
        if (repositorio.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ExcepcionNegocio("nombre", "Ya existe otra materia con ese nombre.");
        }
        mapeador.aplicar(dto, materia);
        return mapeador.aDto(repositorio.save(materia));
    }

    @Override
    public void eliminar(long id) throws ExcepcionNegocio {
        Materia materia = obtener(id);
        if (repositorioNota.existsByMateriaIdAndEliminadoFalse(id)) {
            throw new ExcepcionNegocio("No se puede eliminar la materia \"" + materia.getNombre()
                    + "\": tiene notas cargadas.");
        }
        if (repositorioDocente.existeDocenteConMateria(id)) {
            throw new ExcepcionNegocio("No se puede eliminar la materia \"" + materia.getNombre()
                    + "\": hay docentes que la dictan.");
        }
        materia.setEliminado(true);
        repositorio.save(materia);
    }

    private Materia obtener(long id) throws ExcepcionNegocio {
        return repositorio.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ExcepcionNegocio("La materia solicitada no existe."));
    }
}
