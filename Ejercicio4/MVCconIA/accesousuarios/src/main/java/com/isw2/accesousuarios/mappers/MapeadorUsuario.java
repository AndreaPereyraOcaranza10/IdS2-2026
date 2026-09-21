package com.isw2.accesousuarios.mappers;

import com.isw2.accesousuarios.dtos.RegistroDTO;
import com.isw2.accesousuarios.dtos.UsuarioSesionDTO;
import com.isw2.accesousuarios.entities.Persona;
import com.isw2.accesousuarios.entities.Usuario;
import org.springframework.stereotype.Component;

/**
 * CAPA: MAPPER (conversión DTO <-> Entidad)
 * ============================================================================
 * Centraliza la traducción entre los objetos de la vista (DTOs) y los del modelo
 * (entidades). Así el servicio no mezcla lógica de negocio con copia de campos y
 * las entidades JPA nunca salen de la capa de servicio.
 *
 * ANOTACIÓN
 *   @Component : registra la clase como bean genérico de Spring para poder
 *                inyectarla en el servicio. (@Service, @Repository y @Controller
 *                son especializaciones de @Component.)
 */
@Component
public class MapeadorUsuario {

    /**
     * Construye una Persona NUEVA a partir del formulario de registro. Recorta
     * espacios sobrantes. El correo lo normaliza y asigna el servicio.
     */
    public Persona aPersona(RegistroDTO dto) {
        Persona persona = new Persona();
        persona.setNombre(dto.getNombre().trim());
        persona.setApellido(dto.getApellido().trim());
        persona.setDocumento(dto.getDocumento().trim());
        persona.setFechaNacimiento(dto.getFechaNacimiento());
        persona.setCorreo(dto.getCorreo());
        return persona;
    }

    /**
     * Arma el DTO liviano que se guarda en la sesión. Accede a usuario.getPersona()
     * (carga LAZY), por eso debe invocarse DENTRO de la transacción del servicio.
     */
    public UsuarioSesionDTO aUsuarioSesionDTO(Usuario usuario) {
        Persona persona = usuario.getPersona();
        return new UsuarioSesionDTO(persona.getNombre(), persona.getApellido(), persona.getCorreo());
    }
}
