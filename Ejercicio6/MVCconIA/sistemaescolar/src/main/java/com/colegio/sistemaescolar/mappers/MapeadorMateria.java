package com.colegio.sistemaescolar.mappers;

import com.colegio.sistemaescolar.dtos.MateriaDTO;
import com.colegio.sistemaescolar.entities.Materia;
import com.colegio.sistemaescolar.utils.Textos;
import org.springframework.stereotype.Component;

/** MAPEADOR Entidad <-> DTO de Materia. */
@Component
public class MapeadorMateria {

    public MateriaDTO aDto(Materia materia) {
        MateriaDTO dto = new MateriaDTO();
        dto.setId(materia.getId());
        dto.setNombre(materia.getNombre());
        dto.setDescripcion(materia.getDescripcion());
        return dto;
    }

    public void aplicar(MateriaDTO dto, Materia materia) {
        materia.setNombre(Textos.limpiarNoNulo(dto.getNombre()));
        materia.setDescripcion(Textos.limpiar(dto.getDescripcion()));
    }
}
