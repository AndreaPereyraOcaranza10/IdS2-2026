package com.club.fitnessclub.mappers;

import org.springframework.stereotype.Component;

import com.club.fitnessclub.dtos.PersonaDTO;
import com.club.fitnessclub.entities.Persona;

/**
 * Mapeador Persona (entidad) <-> PersonaDTO.
 *
 * <p>aDTO() usa persona.getGrupoFamiliar() (LAZY) y persona.getImagenRostro() (LAZY):
 * <ul>
 *   <li>getGrupoFamiliar().getNombre() necesita que el grupo esté cargado: las consultas del
 *       repositorio usan @EntityGraph para traerlo, o se ejecuta dentro de la transacción del servicio.</li>
 *   <li>"tieneRostro" solo pregunta si la referencia es null; NO descarga los bytes.</li>
 * </ul>
 *
 * <p>La familia NO se asigna en aEntidad/actualizarEntidad: la resuelve el servicio (necesita
 * validar reglas y buscar la familia en la BD).
 */
@Component
public class MapeadorPersona {

    /** Entidad -> DTO. */
    public PersonaDTO aDTO(Persona persona) {
        PersonaDTO dto = new PersonaDTO();
        dto.setId(persona.getId());
        dto.setNombre(persona.getNombre());
        dto.setApellido(persona.getApellido());
        dto.setDni(persona.getDni());
        dto.setFechaNacimiento(persona.getFechaNacimiento());
        dto.setEmail(persona.getEmail());
        dto.setTelefono(persona.getTelefono());
        dto.setParentesco(persona.getParentesco());
        dto.setGrupoFamiliarId(persona.getGrupoFamiliar().getId());
        dto.setGrupoFamiliarNombre(persona.getGrupoFamiliar().getNombre());
        dto.setTieneRostro(persona.getImagenRostro() != null);
        dto.setActivo(persona.isActivo());
        return dto;
    }

    /** DTO -> entidad NUEVA con los datos personales y el parentesco. La familia la asigna el servicio. */
    public Persona aEntidad(PersonaDTO dto) {
        Persona persona = new Persona();
        copiarDatosPersonales(persona, dto);
        persona.setParentesco(dto.getParentesco());
        return persona;
    }

    /**
     * Modificación: solo se copian los DATOS PERSONALES. Parentesco y familia se mantienen
     * (ver regla del titular en ServicioPersona).
     */
    public void actualizarEntidad(Persona persona, PersonaDTO dto) {
        copiarDatosPersonales(persona, dto);
    }

    private void copiarDatosPersonales(Persona persona, PersonaDTO dto) {
        persona.setNombre(dto.getNombre().trim());
        persona.setApellido(dto.getApellido().trim());
        persona.setDni(dto.getDni().trim());
        persona.setFechaNacimiento(dto.getFechaNacimiento());
        persona.setEmail(dto.getEmail());
        persona.setTelefono(dto.getTelefono());
    }
}
