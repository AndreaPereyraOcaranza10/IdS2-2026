package com.isw2.accesousuarios.services;

import com.isw2.accesousuarios.dtos.LoginDTO;
import com.isw2.accesousuarios.dtos.RegistroDTO;
import com.isw2.accesousuarios.dtos.UsuarioSesionDTO;
import com.isw2.accesousuarios.entities.Persona;
import com.isw2.accesousuarios.entities.Usuario;
import com.isw2.accesousuarios.exceptions.ExcepcionAcceso;
import com.isw2.accesousuarios.exceptions.ExcepcionAcceso.Motivo;
import com.isw2.accesousuarios.mappers.MapeadorUsuario;
import com.isw2.accesousuarios.repositories.RepositorioPersona;
import com.isw2.accesousuarios.repositories.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CAPA: SERVICIO (lógica de negocio)
 * ============================================================================
 * Aquí viven TODAS las reglas del enunciado:
 *
 *   REGISTRO  ({@link #registrar})
 *     - La clave y su confirmación deben coincidir.
 *     - El correo (que es el usuario) no puede estar ya registrado.
 *     - El documento no puede estar ya registrado.
 *     - La clave se guarda con hash BCrypt, nunca en texto plano.
 *
 *   INGRESO   ({@link #autenticar})
 *     - Si el correo no está registrado           -> NO_REGISTRADO
 *       (el controlador redirige al formulario de registro).
 *     - Si la cuenta está bloqueada               -> CUENTA_BLOQUEADA.
 *     - Si la clave es incorrecta                 -> se suma un intento fallido;
 *       al llegar a 3 la cuenta se BLOQUEA         -> CLAVE_INCORRECTA / CUENTA_BLOQUEADA.
 *     - Si la clave es correcta                   -> el contador vuelve a 0.
 *
 * Interpretación adoptada del enunciado: los 3 errores son CONSECUTIVOS; un
 * ingreso correcto reinicia el conteo. El desbloqueo de cuentas no forma parte
 * del enunciado, por lo que no se implementa.
 *
 * Convención del proyecto: los métodos declaran "throws Exception". Las reglas
 * de negocio que fallan lanzan {@link ExcepcionAcceso} (una Exception con motivo).
 *
 * ANOTACIONES
 *   @Service            : bean de la capa de negocio (especialización de @Component).
 *   @RequiredArgsConstructor (Lombok): genera un constructor con los campos final
 *                         SIN inicializar (los tres colaboradores). Spring lo usa
 *                         para INYECCIÓN POR CONSTRUCTOR, la forma recomendada:
 *                         deja las dependencias inmutables y facilita los tests.
 *   @Transactional      : ver cada método.
 */
@Service
@RequiredArgsConstructor
public class ServicioUsuario {

    /** Cantidad de claves incorrectas consecutivas que provoca el bloqueo. */
    public static final int MAXIMO_INTENTOS_FALLIDOS = 3;

    private final RepositorioPersona repositorioPersona;
    private final RepositorioUsuario repositorioUsuario;
    private final MapeadorUsuario mapeador;

    /**
     * Codificador BCrypt (de spring-security-crypto). Es SOLO una función de hash
     * con sal: no activa ni configura Spring Security. Al tener valor inicial,
     * Lombok lo excluye del constructor generado.
     */
    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    // =========================================================================
    // REGISTRO
    // =========================================================================

    /**
     * Registra una persona nueva junto con su usuario.
     *
     * @Transactional: todo el método corre en UNA transacción. Si ocurre cualquier
     * error, no se guarda ni la Persona ni el Usuario (todo o nada). Ante una
     * excepción checked como ExcepcionAcceso, Spring hace commit por defecto, pero
     * aquí todas las validaciones se hacen ANTES de guardar, así que no hay nada
     * a medio escribir.
     *
     * @return datos básicos del usuario recien registrado
     * @throws ExcepcionAcceso claves distintas, correo duplicado o documento duplicado
     */
    @Transactional
    public UsuarioSesionDTO registrar(RegistroDTO dto) throws Exception {
        String correo = normalizarCorreo(dto.getCorreo());
        String documento = dto.getDocumento().trim();

        if (!dto.getClave().equals(dto.getConfirmarClave())) {
            throw new ExcepcionAcceso(Motivo.CLAVES_NO_COINCIDEN, "Las claves no coinciden.");
        }
        if (repositorioPersona.existsByCorreo(correo)) {
            throw new ExcepcionAcceso(Motivo.CORREO_DUPLICADO,
                    "Ese correo ya está registrado. Ingrese al sistema con su clave.");
        }
        if (repositorioPersona.existsByDocumento(documento)) {
            throw new ExcepcionAcceso(Motivo.DOCUMENTO_DUPLICADO,
                    "Ya existe una persona registrada con ese documento.");
        }

        Persona persona = mapeador.aPersona(dto);
        persona.setCorreo(correo);
        persona.setDocumento(documento);

        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setClave(codificador.encode(dto.getClave()));
        usuario.setIntentosFallidos(0);
        usuario.setBloqueado(false);

        // cascade=PERSIST en Usuario.persona: este único save inserta también la Persona.
        repositorioUsuario.save(usuario);

        return mapeador.aUsuarioSesionDTO(usuario);
    }

    // =========================================================================
    // INGRESO
    // =========================================================================

    /**
     * Verifica correo y clave aplicando la regla de los 3 intentos.
     *
     * @Transactional(noRollbackFor = ExcepcionAcceso.class): en este método hay que
     * GUARDAR el nuevo contador de intentos (o el bloqueo) Y ADEMÁS lanzar la
     * excepción para avisar al controlador. Si la transacción se revirtiera por la
     * excepción, el intento fallido se perdería y la cuenta nunca se bloquearía.
     * Por defecto Spring solo revierte ante excepciones NO checked (RuntimeException)
     * y ExcepcionAcceso es checked, así que ya se confirmaría; noRollbackFor deja
     * esa decisión EXPLÍCITA y protege el comportamiento si la excepción cambiara
     * de tipo en el futuro.
     *
     * @return datos básicos del usuario autenticado (para guardar en la sesión)
     * @throws ExcepcionAcceso NO_REGISTRADO, CUENTA_BLOQUEADA o CLAVE_INCORRECTA
     */
    @Transactional(noRollbackFor = ExcepcionAcceso.class)
    public UsuarioSesionDTO autenticar(LoginDTO dto) throws Exception {
        String correo = normalizarCorreo(dto.getCorreo());

        // 1) ¿Esta registrado? Si no, el controlador deriva al registro.
        Usuario usuario = repositorioUsuario.findByPersonaCorreo(correo)
                .orElseThrow(() -> new ExcepcionAcceso(Motivo.NO_REGISTRADO,
                        "El correo " + correo + " no está registrado. Complete el formulario para registrarse."));

        // 2) ¿Ya estaba bloqueado? Ni siquiera se evalua la clave.
        if (usuario.isBloqueado()) {
            throw new ExcepcionAcceso(Motivo.CUENTA_BLOQUEADA,
                    "Su cuenta está bloqueada por superar los " + MAXIMO_INTENTOS_FALLIDOS
                            + " intentos fallidos.");
        }

        // 3) Comparación de la clave ingresada contra el hash guardado.
        if (!codificador.matches(dto.getClave(), usuario.getClave())) {
            int intentos = usuario.getIntentosFallidos() + 1;
            usuario.setIntentosFallidos(intentos);

            if (intentos >= MAXIMO_INTENTOS_FALLIDOS) {
                usuario.setBloqueado(true);
                repositorioUsuario.save(usuario);
                throw new ExcepcionAcceso(Motivo.CUENTA_BLOQUEADA,
                        "Clave incorrecta. Su cuenta fue bloqueada tras " + MAXIMO_INTENTOS_FALLIDOS
                                + " intentos fallidos.");
            }

            repositorioUsuario.save(usuario);
            int restantes = MAXIMO_INTENTOS_FALLIDOS - intentos;
            throw new ExcepcionAcceso(Motivo.CLAVE_INCORRECTA,
                    "Clave incorrecta. " + (restantes == 1
                            ? "Le queda 1 intento antes de que se bloquee la cuenta."
                            : "Le quedan " + restantes + " intentos antes de que se bloquee la cuenta."));
        }

        // 4) Clave correcta: se reinicia el contador de errores consecutivos.
        if (usuario.getIntentosFallidos() != 0) {
            usuario.setIntentosFallidos(0);
            repositorioUsuario.save(usuario);
        }
        return mapeador.aUsuarioSesionDTO(usuario);
    }

    /** Recorta espacios y pasa a minúsculas para que "Ana@Mail.com " y "ana@mail.com" sean el mismo usuario. */
    private static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }
}
