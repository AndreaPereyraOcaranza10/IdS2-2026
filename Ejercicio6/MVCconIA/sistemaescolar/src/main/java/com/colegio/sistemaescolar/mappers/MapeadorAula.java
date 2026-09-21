package com.colegio.sistemaescolar.mappers;

import com.colegio.sistemaescolar.dtos.AulaDTO;
import com.colegio.sistemaescolar.entities.Aula;
import com.colegio.sistemaescolar.utils.Textos;
import org.springframework.stereotype.Component;

/** MAPEADOR Entidad <-> DTO de Aula. */
@Component
public class MapeadorAula {

    public AulaDTO aDto(Aula aula) {
        AulaDTO dto = new AulaDTO();
        dto.setId(aula.getId());
        dto.setCodigo(aula.getCodigo());
        dto.setCapacidad(aula.getCapacidad());
        dto.setUbicacion(aula.getUbicacion());
        return dto;
    }

    public void aplicar(AulaDTO dto, Aula aula) {
        aula.setCodigo(Textos.limpiarNoNulo(dto.getCodigo()));
        aula.setCapacidad(dto.getCapacidad());
        aula.setUbicacion(Textos.limpiar(dto.getUbicacion()));
    }
}
