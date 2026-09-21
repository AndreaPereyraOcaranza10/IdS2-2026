package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.dtos.CambioPasswordDTO;
import com.colegio.sistemaescolar.dtos.PerfilDTO;
import com.colegio.sistemaescolar.entities.Docente;
import com.colegio.sistemaescolar.entities.Usuario;
import com.colegio.sistemaescolar.events.PasswordCambiadaEvento;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.repositories.RepositorioUsuario;
import com.colegio.sistemaescolar.security.ContextoUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * SERVICIO de la cuenta del usuario autenticado: consulta de perfil y <b>cambio de contraseña</b> (requisito del enunciado).
 *
 * <h2>Reglas de negocio del cambio de contraseña</h2>
 * <ol>
 *   <li>Siempre se cambia la contraseña de QUIEN ESTÁ LOGUEADO (se toma del contexto de seguridad, nunca de un
 *       parámetro del formulario: así nadie puede cambiar la clave de otro usuario manipulando la petición).</li>
 *   <li>Se exige la contraseña actual (protege si alguien deja la sesión abierta).</li>
 *   <li>La nueva debe cumplir la política (8-72 caracteres, letra y número), coincidir con su confirmación y ser distinta de la actual.</li>
 *   <li>Se guarda con BCrypt y se registra la fecha del cambio; se publica {@link PasswordCambiadaEvento} para avisar por correo.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ServicioCuenta {

    private final RepositorioUsuario repositorioUsuario;
    private final ServicioAcceso acceso;
    private final ContextoUsuario contexto;
    private final PasswordEncoder codificador;
    private final ApplicationEventPublisher publicador;
    private final Clock reloj;

    @Transactional(readOnly = true)
    public PerfilDTO perfil() throws ExcepcionNegocio {
        Usuario usuario = usuarioActual();
        PerfilDTO dto = new PerfilDTO();
        dto.setEmail(usuario.getEmail());
        dto.setRolEtiqueta(usuario.getRol().getEtiqueta());
        dto.setNombreCompleto(nombreVisible(usuario));
        dto.setUltimoCambioPassword(usuario.getUltimoCambioPassword());
        return dto;
    }

    public void cambiarPassword(CambioPasswordDTO dto) throws ExcepcionNegocio {
        Usuario usuario = usuarioActual();

        if (!codificador.matches(dto.getPasswordActual(), usuario.getPasswordHash())) {
            throw new ExcepcionNegocio("passwordActual", "La contraseña actual es incorrecta.");
        }
        if (!dto.getPasswordNueva().equals(dto.getConfirmarPassword())) {
            throw new ExcepcionNegocio("confirmarPassword", "La confirmación no coincide con la nueva contraseña.");
        }
        if (dto.getPasswordNueva().equals(dto.getPasswordActual())) {
            throw new ExcepcionNegocio("passwordNueva", "La nueva contraseña debe ser distinta de la actual.");
        }

        usuario.setPasswordHash(codificador.encode(dto.getPasswordNueva()));
        usuario.setUltimoCambioPassword(LocalDateTime.now(reloj));
        repositorioUsuario.save(usuario);

        publicador.publishEvent(new PasswordCambiadaEvento(usuario.getEmail(), nombreVisible(usuario)));
    }

    private Usuario usuarioActual() throws ExcepcionNegocio {
        String email = contexto.email()
                .orElseThrow(() -> new ExcepcionNegocio("Debe iniciar sesión."));
        return repositorioUsuario.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ExcepcionNegocio("No se encontró la cuenta."));
    }

    private String nombreVisible(Usuario usuario) {
        return acceso.docenteActual()
                .map(this::nombreDe)
                .orElse("Administración");
    }

    private String nombreDe(Docente docente) {
        return docente.getNombre() + " " + docente.getApellido();
    }
}
