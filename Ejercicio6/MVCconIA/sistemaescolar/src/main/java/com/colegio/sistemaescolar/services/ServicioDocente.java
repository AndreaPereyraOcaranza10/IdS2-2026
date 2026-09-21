package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.config.PropiedadesColegio;
import com.colegio.sistemaescolar.dtos.DocenteDTO;
import com.colegio.sistemaescolar.dtos.RegistroDocenteDTO;
import com.colegio.sistemaescolar.entities.AsignacionDocente;
import com.colegio.sistemaescolar.entities.Docente;
import com.colegio.sistemaescolar.entities.Grado;
import com.colegio.sistemaescolar.entities.Materia;
import com.colegio.sistemaescolar.entities.Usuario;
import com.colegio.sistemaescolar.enums.Rol;
import com.colegio.sistemaescolar.events.DocenteRegistradoEvento;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.mappers.MapeadorDocente;
import com.colegio.sistemaescolar.repositories.RepositorioAsignacionDocente;
import com.colegio.sistemaescolar.repositories.RepositorioDocente;
import com.colegio.sistemaescolar.repositories.RepositorioGrado;
import com.colegio.sistemaescolar.repositories.RepositorioMateria;
import com.colegio.sistemaescolar.repositories.RepositorioUsuario;
import com.colegio.sistemaescolar.utils.Textos;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SERVICIO de Docentes: registro (con usuario y contraseña), administración y asignación de grados/materias.
 *
 * <h2>Reglas de negocio</h2>
 * <ul>
 *   <li>El docente se registra con Nombre, Apellido, Sexo y Fecha de Nacimiento (enunciado).</li>
 *   <li>El <b>usuario es el correo personal</b>: debe ser único en todo el sistema (también respecto del ADMIN).</li>
 *   <li>La contraseña se guarda SIEMPRE cifrada con BCrypt; nunca en texto plano.</li>
 *   <li>Al registrarse se crean dos filas en una sola transacción: {@link Usuario} (credenciales) y {@link Docente} (datos personales).
 *       Si algo falla, no queda ninguna de las dos.</li>
 *   <li>Al registrarse se publica un {@link DocenteRegistradoEvento}: el listener envía el correo de bienvenida.</li>
 *   <li>Según {@code colegio.registro.requiere-aprobacion}, la cuenta nace habilitada o pendiente de habilitación del ADMIN.</li>
 *   <li>Un docente recién registrado no tiene asignaciones (no ve alumnos) hasta que el ADMIN se las asigne.</li>
 *   <li>Cada asignación es una terna docente + grado + materia ({@link AsignacionDocente}): permite que dicte
 *       Matemática en 1.º Grado y no en 2.º.</li>
 * </ul>
 *
 * <h2>Nota de diseño</h2>
 * No implementa {@link ServicioBase} porque crear un docente requiere contraseña (otro DTO: {@link RegistroDocenteDTO})
 * y editar uno no puede tocar sus credenciales. Se documenta la diferencia en vez de forzar una interfaz que no encaja.
 *
 * <h2>Anotaciones</h2>
 * {@code @Transactional(rollbackFor = Exception.class)}: también revierte ante excepciones verificadas
 * ({@link ExcepcionNegocio}); por defecto Spring solo revierte ante {@code RuntimeException}.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ServicioDocente {

    private final RepositorioDocente repositorio;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioGrado repositorioGrado;
    private final RepositorioMateria repositorioMateria;
    private final RepositorioAsignacionDocente repositorioAsignacion;
    private final MapeadorDocente mapeador;
    private final PasswordEncoder codificador;
    private final ApplicationEventPublisher publicador;
    private final PropiedadesColegio propiedades;
    private final Clock reloj;

    @Transactional(readOnly = true)
    public List<DocenteDTO> listar() {
        // Una sola consulta para todas las asignaciones, agrupadas luego por docente (evita una consulta por fila).
        Map<Long, List<AsignacionDocente>> porDocente = new HashMap<>();
        for (AsignacionDocente asignacion : repositorioAsignacion.findByDocenteEliminadoFalse()) {
            porDocente.computeIfAbsent(asignacion.getDocente().getId(), k -> new ArrayList<>()).add(asignacion);
        }
        List<DocenteDTO> resultado = new ArrayList<>();
        for (Docente docente : repositorio.findByEliminadoFalseOrderByApellidoAscNombreAsc()) {
            resultado.add(mapeador.aDto(docente, porDocente.getOrDefault(docente.getId(), new ArrayList<>())));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public DocenteDTO buscarPorId(long id) throws ExcepcionNegocio {
        Docente docente = obtener(id);
        return mapeador.aDto(docente, repositorioAsignacion.findByDocenteId(docente.getId()));
    }

    /**
     * Registra un docente nuevo (autoregistro público o alta por el ADMIN: ambos usan este método).
     *
     * @return el docente creado, como DTO
     */
    public DocenteDTO registrar(RegistroDocenteDTO dto) throws ExcepcionNegocio {
        if (!dto.getPassword().equals(dto.getConfirmarPassword())) {
            throw new ExcepcionNegocio("confirmarPassword", "Las contraseñas no coinciden.");
        }
        String email = Textos.limpiarNoNulo(dto.getEmail()).toLowerCase();
        if (repositorioUsuario.existsByEmailIgnoreCase(email)) {
            throw new ExcepcionNegocio("email", "Ya existe una cuenta con ese correo.");
        }

        boolean pendiente = propiedades.getRegistro().isRequiereAprobacion();

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPasswordHash(codificador.encode(dto.getPassword()));   // BCrypt
        usuario.setRol(Rol.DOCENTE);                                      // el registro público JAMÁS crea ADMIN
        usuario.setHabilitado(!pendiente);
        usuario.setUltimoCambioPassword(LocalDateTime.now(reloj));
        usuario = repositorioUsuario.save(usuario);

        Docente docente = new Docente();
        docente.setNombre(Textos.limpiarNoNulo(dto.getNombre()));
        docente.setApellido(Textos.limpiarNoNulo(dto.getApellido()));
        docente.setSexo(dto.getSexo());
        docente.setFechaNacimiento(dto.getFechaNacimiento());
        docente.setUsuario(usuario);
        docente = repositorio.save(docente);

        // El listener envía el correo de bienvenida DESPUÉS de confirmada la transacción.
        publicador.publishEvent(new DocenteRegistradoEvento(
                email, docente.getNombre() + " " + docente.getApellido(), pendiente));
        return mapeador.aDto(docente, new ArrayList<>());   // recién creado: sin asignaciones
    }

    /**
     * Actualiza datos personales y asignaciones (docente + grado + materia). El correo (usuario) no se modifica desde acá.
     * Las asignaciones se sincronizan por diferencia: se crean las nuevas y se borran las desmarcadas.
     */
    public DocenteDTO actualizar(long id, DocenteDTO dto) throws ExcepcionNegocio {
        Docente docente = obtener(id);
        mapeador.aplicarDatosPersonales(dto, docente);

        // 1) Asignaciones pedidas: cada texto "gradoId-materiaId" se valida contra la base.
        Map<String, AsignacionDocente> pedidas = new HashMap<>();
        for (String clave : dto.getAsignaciones() == null ? new HashSet<String>() : dto.getAsignaciones()) {
            long[] ids = interpretarClave(clave);
            Grado grado = repositorioGrado.findByIdAndEliminadoFalse(ids[0])
                    .orElseThrow(() -> new ExcepcionNegocio("Alguno de los grados seleccionados ya no existe."));
            Materia materia = repositorioMateria.findByIdAndEliminadoFalse(ids[1])
                    .orElseThrow(() -> new ExcepcionNegocio("Alguna de las materias seleccionadas ya no existe."));
            AsignacionDocente nueva = new AsignacionDocente();
            nueva.setDocente(docente);
            nueva.setGrado(grado);
            nueva.setMateria(materia);
            pedidas.put(MapeadorDocente.clave(ids[0], ids[1]), nueva);
        }

        // 2) Sincroniza: borra las que ya no están marcadas y agrega las nuevas.
        List<AsignacionDocente> existentes = repositorioAsignacion.findByDocenteId(docente.getId());
        List<AsignacionDocente> vigentes = new ArrayList<>();
        Set<String> clavesExistentes = new HashSet<>();
        for (AsignacionDocente existente : existentes) {
            String clave = MapeadorDocente.clave(existente.getGrado().getId(), existente.getMateria().getId());
            if (pedidas.containsKey(clave)) {
                clavesExistentes.add(clave);
                vigentes.add(existente);
            } else {
                repositorioAsignacion.delete(existente);
            }
        }
        for (Map.Entry<String, AsignacionDocente> entrada : pedidas.entrySet()) {
            if (!clavesExistentes.contains(entrada.getKey())) {
                vigentes.add(repositorioAsignacion.save(entrada.getValue()));
            }
        }

        docente.getUsuario().setHabilitado(dto.isHabilitado());
        return mapeador.aDto(repositorio.save(docente), vigentes);
    }

    /** Habilita o deshabilita la cuenta (un usuario deshabilitado no puede iniciar sesión). */
    public void cambiarHabilitacion(long id, boolean habilitado) throws ExcepcionNegocio {
        Docente docente = obtener(id);
        docente.getUsuario().setHabilitado(habilitado);
        repositorio.save(docente);
    }

    /** Baja lógica: el docente y su usuario quedan marcados como eliminados y deshabilitados (no se borra la fila). */
    public void eliminar(long id) throws ExcepcionNegocio {
        Docente docente = obtener(id);
        docente.setEliminado(true);
        docente.getUsuario().setEliminado(true);
        docente.getUsuario().setHabilitado(false);
        repositorio.save(docente);
    }

    private Docente obtener(long id) throws ExcepcionNegocio {
        return repositorio.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ExcepcionNegocio("El docente solicitado no existe."));
    }

    /** Convierte "gradoId-materiaId" en sus dos números; rechaza cualquier formato inesperado (formulario manipulado). */
    private long[] interpretarClave(String clave) throws ExcepcionNegocio {
        try {
            String[] partes = clave.split("-");
            if (partes.length != 2) {
                throw new NumberFormatException();
            }
            return new long[]{Long.parseLong(partes[0]), Long.parseLong(partes[1])};
        } catch (NumberFormatException e) {
            throw new ExcepcionNegocio("Las asignaciones enviadas no son válidas.");
        }
    }
}
