package com.club.fitnessclub.services;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.club.fitnessclub.dtos.UsuarioDTO;
import com.club.fitnessclub.entities.Usuario;
import com.club.fitnessclub.enums.Rol;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.mappers.MapeadorUsuario;
import com.club.fitnessclub.repositories.RepositorioUsuario;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de USUARIOS del sistema (ABM, solo accesible para ADMIN).
 *
 * <p>REGLAS DE NEGOCIO:
 * <ol>
 *   <li>El username es único (incluso frente a usuarios dados de baja).</li>
 *   <li>Contraseña OBLIGATORIA en el alta (mínimo 8 caracteres) y OPCIONAL en la modificación
 *       (vacía = se conserva la actual). Debe coincidir con su confirmación.</li>
 *   <li>La contraseña se guarda hasheada con BCrypt (PasswordEncoder), nunca en texto plano.</li>
 *   <li>Siempre debe quedar al menos un ADMIN vigente: no se puede dar de baja ni degradar al último.</li>
 * </ol>
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ServicioUsuario implements ServicioBase<UsuarioDTO> {

    static final int LONGITUD_MINIMA_PASSWORD = 8;

    private final RepositorioUsuario repositorioUsuario;
    private final MapeadorUsuario mapeador;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<UsuarioDTO> findAll() throws Exception {
        return repositorioUsuario.findByActivoTrueOrderByUsernameAsc().stream().map(mapeador::aDTO).toList();
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public UsuarioDTO findById(long id) throws Exception {
        return mapeador.aDTO(buscarActivo(id));
    }

    @Override
    public UsuarioDTO save(UsuarioDTO dto) throws Exception {
        String username = dto.getUsername().trim();
        if (repositorioUsuario.existsByUsername(username)) {
            throw new ExcepcionNegocio("Ya existe un usuario con el nombre \"" + username + "\".");
        }
        // Regla 2 (alta): contraseña obligatoria.
        if (esVacia(dto.getPassword())) {
            throw new ExcepcionNegocio("La contraseña es obligatoria para crear un usuario.");
        }
        validarPassword(dto);

        Usuario usuario = mapeador.aEntidad(dto);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        return mapeador.aDTO(repositorioUsuario.save(usuario));
    }

    @Override
    public UsuarioDTO update(long id, UsuarioDTO dto) throws Exception {
        Usuario usuario = buscarActivo(id);

        if (repositorioUsuario.existsByUsernameAndIdNot(dto.getUsername().trim(), id)) {
            throw new ExcepcionNegocio("Ya existe otro usuario con el nombre \"" + dto.getUsername().trim() + "\".");
        }
        // Regla 4: no degradar al último ADMIN.
        if (usuario.getRol() == Rol.ADMIN && dto.getRol() != Rol.ADMIN
                && repositorioUsuario.countByRolAndActivoTrue(Rol.ADMIN) <= 1) {
            throw new ExcepcionNegocio("Debe existir al menos un administrador: no se puede cambiar el rol del último ADMIN.");
        }

        mapeador.actualizarEntidad(usuario, dto);

        // Regla 2 (modificación): contraseña opcional.
        if (!esVacia(dto.getPassword())) {
            validarPassword(dto);
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        return mapeador.aDTO(repositorioUsuario.save(usuario));
    }

    /** BAJA LÓGICA (regla 4: nunca se da de baja al último ADMIN). */
    @Override
    public void delete(long id) throws Exception {
        Usuario usuario = buscarActivo(id);
        if (usuario.getRol() == Rol.ADMIN && repositorioUsuario.countByRolAndActivoTrue(Rol.ADMIN) <= 1) {
            throw new ExcepcionNegocio("Debe existir al menos un administrador: no se puede dar de baja al último ADMIN.");
        }
        usuario.setActivo(false);
        repositorioUsuario.save(usuario);
    }

    private Usuario buscarActivo(long id) throws ExcepcionNegocio {
        return repositorioUsuario.findById(id)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new ExcepcionNegocio("El usuario solicitado no existe o fue dado de baja."));
    }

    /** Longitud mínima y coincidencia con la confirmación. */
    private void validarPassword(UsuarioDTO dto) throws ExcepcionNegocio {
        if (dto.getPassword().length() < LONGITUD_MINIMA_PASSWORD) {
            throw new ExcepcionNegocio("La contraseña debe tener al menos " + LONGITUD_MINIMA_PASSWORD + " caracteres.");
        }
        if (!dto.getPassword().equals(dto.getConfirmarPassword())) {
            throw new ExcepcionNegocio("La contraseña y su confirmación no coinciden.");
        }
    }

    private boolean esVacia(String texto) {
        return texto == null || texto.isBlank();
    }
}
