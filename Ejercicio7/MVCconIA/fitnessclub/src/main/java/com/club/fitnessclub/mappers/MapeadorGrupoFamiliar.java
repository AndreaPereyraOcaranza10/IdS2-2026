package com.club.fitnessclub.mappers;

import org.springframework.stereotype.Component;

import com.club.fitnessclub.dtos.GrupoFamiliarDTO;
import com.club.fitnessclub.entities.GrupoFamiliar;
import com.club.fitnessclub.entities.Persona;
import com.club.fitnessclub.enums.Parentesco;

/**
 * Mapeador GrupoFamiliar (entidad) <-> GrupoFamiliarDTO.
 *
 * <p>Es un componente sin estado: convierte, no decide. Las reglas de negocio viven en el servicio.
 *
 * <p>ATENCIÓN: aDTO() recorre la colección LAZY "integrantes", por lo que debe ejecutarse DENTRO
 * de una transacción (los métodos del servicio lo garantizan).
 */
@Component
public class MapeadorGrupoFamiliar {

    /** Entidad -> DTO (incluye los campos calculados de solo lectura). */
    public GrupoFamiliarDTO aDTO(GrupoFamiliar grupo) {
        GrupoFamiliarDTO dto = new GrupoFamiliarDTO();
        dto.setId(grupo.getId());
        dto.setNombre(grupo.getNombre());
        dto.setDireccion(grupo.getDireccion());
        dto.setTelefono(grupo.getTelefono());
        dto.setActivo(grupo.isActivo());

        // Cantidad de integrantes VIGENTES (las bajas lógicas no cuentan).
        dto.setCantidadIntegrantes((int) grupo.getIntegrantes().stream()
                .filter(Persona::isActivo)
                .count());

        // Titular = integrante vigente con parentesco TITULAR (puede no existir aún).
        grupo.getIntegrantes().stream()
                .filter(p -> p.isActivo() && p.getParentesco() == Parentesco.TITULAR)
                .findFirst()
                .ifPresent(t -> dto.setTitularNombre(t.getNombreCompleto()));

        return dto;
    }

    /** DTO -> entidad NUEVA (alta). No copia id ni campos calculados. */
    public GrupoFamiliar aEntidad(GrupoFamiliarDTO dto) {
        GrupoFamiliar grupo = new GrupoFamiliar();
        copiarCampos(grupo, dto);
        return grupo;
    }

    /** Vuelca los datos editables del DTO sobre una entidad ya existente (modificación). */
    public void actualizarEntidad(GrupoFamiliar grupo, GrupoFamiliarDTO dto) {
        copiarCampos(grupo, dto);
    }

    private void copiarCampos(GrupoFamiliar grupo, GrupoFamiliarDTO dto) {
        grupo.setNombre(dto.getNombre().trim());
        grupo.setDireccion(dto.getDireccion());
        grupo.setTelefono(dto.getTelefono());
    }
}
