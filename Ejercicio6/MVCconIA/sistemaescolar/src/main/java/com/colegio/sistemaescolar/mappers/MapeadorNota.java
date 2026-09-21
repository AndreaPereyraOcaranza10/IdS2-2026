package com.colegio.sistemaescolar.mappers;

import com.colegio.sistemaescolar.dtos.NotaDTO;
import com.colegio.sistemaescolar.entities.Nota;
import com.colegio.sistemaescolar.utils.Textos;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * MAPEADOR Entidad <-> DTO de Nota. Se invoca dentro de la transacción (lee alumno y materia LAZY).
 */
@Component
public class MapeadorNota {

    /** Nota mínima para aprobar. */
    public static final BigDecimal NOTA_APROBACION = new BigDecimal("6");

    public NotaDTO aDto(Nota nota) {
        NotaDTO dto = new NotaDTO();
        dto.setId(nota.getId());
        dto.setAlumnoId(nota.getAlumno().getId());
        dto.setMateriaId(nota.getMateria().getId());
        dto.setPeriodo(nota.getPeriodo());
        dto.setValor(nota.getValor());
        dto.setFecha(nota.getFecha());
        dto.setObservaciones(nota.getObservaciones());
        dto.setAlumnoNombre(nota.getAlumno().getApellido() + ", " + nota.getAlumno().getNombre());
        dto.setGradoNombre(nota.getAlumno().getGrado().getNombre());
        dto.setMateriaNombre(nota.getMateria().getNombre());
        dto.setPeriodoEtiqueta(nota.getPeriodo().getEtiqueta());
        dto.setAprobada(nota.getValor().compareTo(NOTA_APROBACION) >= 0);
        dto.setCargadaPor(nota.getCreadoPor());
        return dto;
    }

    /** Copia los datos escalares; alumno y materia los asigna el servicio tras validar permisos. */
    public void aplicar(NotaDTO dto, Nota nota) {
        nota.setPeriodo(dto.getPeriodo());
        nota.setValor(dto.getValor());
        nota.setFecha(dto.getFecha());
        nota.setObservaciones(Textos.limpiar(dto.getObservaciones()));
    }
}
