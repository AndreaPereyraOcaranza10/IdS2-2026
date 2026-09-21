package com.colegio.sistemaescolar.mappers;

import com.colegio.sistemaescolar.dtos.GradoDTO;
import com.colegio.sistemaescolar.entities.Grado;
import com.colegio.sistemaescolar.utils.Textos;
import org.springframework.stereotype.Component;

/**
 * MAPEADOR Entidad <-> DTO de Grado.
 *
 * <p>Es la frontera entre el modelo de persistencia y el resto de la aplicación: el servicio usa el
 * mapeador para convertir, así ninguna entidad JPA llega al controlador ni a la vista.
 * {@code @Component} lo registra como bean para poder inyectarlo.</p>
 */
@Component
public class MapeadorGrado {

    public GradoDTO aDto(Grado grado) {
        GradoDTO dto = new GradoDTO();
        dto.setId(grado.getId());
        dto.setNombre(grado.getNombre());
        dto.setNivel(grado.getNivel());
        return dto;
    }

    /** Copia los datos editables del DTO a la entidad (no toca id ni auditoría). */
    public void aplicar(GradoDTO dto, Grado grado) {
        grado.setNombre(Textos.limpiarNoNulo(dto.getNombre()));
        grado.setNivel(dto.getNivel());
    }
}
