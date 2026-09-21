package com.club.fitnessclub.config;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.club.fitnessclub.entities.Cuota;
import com.club.fitnessclub.entities.GrupoFamiliar;
import com.club.fitnessclub.entities.Persona;
import com.club.fitnessclub.entities.Usuario;
import com.club.fitnessclub.enums.EstadoCuota;
import com.club.fitnessclub.enums.Parentesco;
import com.club.fitnessclub.enums.Rol;
import com.club.fitnessclub.repositories.RepositorioCuota;
import com.club.fitnessclub.repositories.RepositorioGrupoFamiliar;
import com.club.fitnessclub.repositories.RepositorioUsuario;

import lombok.RequiredArgsConstructor;

/**
 * Carga inicial de datos (se ejecuta una vez al arrancar la aplicación).
 *
 * <p>CommandLineRunner: Spring invoca run() cuando el contexto ya está listo (tablas creadas por Hibernate).
 * Es IDEMPOTENTE: solo carga datos si las tablas están vacías, por lo que reiniciar no duplica nada.
 *
 * <p>Se usa un runner Java y no data.sql porque el hash BCrypt de las contraseñas debe generarse con
 * el PasswordEncoder real, y porque así los datos pasan por las mismas entidades (y la auditoría
 * registra "SISTEMA" como creador).
 *
 * <p>Qué crea:
 * <ul>
 *   <li>Usuarios: admin / admin1234 (ADMIN) y recepcion / recepcion1234 (RECEPCION). CAMBIAR en producción.</li>
 *   <li>N familias de demostración (club.datos-demo.cantidad-familias), de 3 integrantes cada una
 *       (titular, cónyuge, hijo/a) con DNIs consecutivos desde 30000001, y su cuota del mes en curso.
 *       Cada 3 familias, una además tiene una cuota del mes anterior VENCIDA sin pagar (para probar la
 *       advertencia en recepción).</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class InicializadorDatos implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorDatos.class);

    private static final long DNI_INICIAL = 30_000_000L;
    private static final String[] APELLIDOS = { "Pérez", "Gómez", "Rodríguez", "Fernández", "López", "Martínez",
            "Sánchez", "Romero", "Díaz", "Torres", "Ruiz", "Álvarez", "Benítez", "Acosta", "Medina" };
    private static final String[] NOMBRES_TITULAR = { "Juan", "María", "Carlos", "Laura", "Diego", "Ana", "Pablo", "Sofía" };
    private static final String[] NOMBRES_CONYUGE = { "Lucía", "Martín", "Valeria", "Sergio", "Carla", "Hernán", "Julia", "Nicolás" };
    private static final String[] NOMBRES_HIJO = { "Mateo", "Emma", "Thiago", "Olivia", "Benjamín", "Mía", "Lautaro", "Valentina" };

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioGrupoFamiliar repositorioGrupo;
    private final RepositorioCuota repositorioCuota;
    private final PasswordEncoder passwordEncoder;
    private final Clock reloj;

    @Value("${club.datos-demo.cantidad-familias:5}")
    private int cantidadFamilias;

    @Value("${club.cuota.importe-sugerido:25000.00}")
    private BigDecimal importeCuota;

    @Override
    public void run(String... args) {
        crearUsuariosIniciales();
        crearFamiliasDemo();
    }

    private void crearUsuariosIniciales() {
        if (repositorioUsuario.count() > 0) {
            return;
        }
        repositorioUsuario.save(crearUsuario("admin", "admin1234", Rol.ADMIN));
        repositorioUsuario.save(crearUsuario("recepcion", "recepcion1234", Rol.RECEPCION));
        log.warn("Usuarios iniciales creados: admin/admin1234 y recepcion/recepcion1234. Cambie las contraseñas.");
    }

    private Usuario crearUsuario(String username, String password, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        return usuario;
    }

    private void crearFamiliasDemo() {
        if (cantidadFamilias <= 0 || repositorioGrupo.count() > 0) {
            return;
        }
        LocalDate hoy = LocalDate.now(reloj);
        LocalDate mesActual = YearMonth.from(hoy).atDay(1);
        long dni = DNI_INICIAL;

        for (int i = 0; i < cantidadFamilias; i++) {
            String apellido = APELLIDOS[i % APELLIDOS.length] + (i >= APELLIDOS.length ? " " + (i / APELLIDOS.length + 1) : "");

            GrupoFamiliar grupo = new GrupoFamiliar();
            grupo.setNombre("Familia " + apellido);
            grupo.setDireccion("Calle Demo " + (100 + i) + ", Mendoza");
            grupo.setTelefono("261-555-" + String.format("%04d", i));

            grupo.agregarIntegrante(crearPersona(NOMBRES_TITULAR[i % NOMBRES_TITULAR.length], apellido, ++dni, Parentesco.TITULAR, 1980));
            grupo.agregarIntegrante(crearPersona(NOMBRES_CONYUGE[i % NOMBRES_CONYUGE.length], apellido, ++dni, Parentesco.CONYUGE, 1982));
            grupo.agregarIntegrante(crearPersona(NOMBRES_HIJO[i % NOMBRES_HIJO.length], apellido, ++dni, Parentesco.HIJO, 2012));

            // Cascade ALL: guardar el grupo guarda también a sus tres integrantes.
            GrupoFamiliar guardado = repositorioGrupo.save(grupo);

            repositorioCuota.save(crearCuota(guardado, mesActual, mesActual.plusDays(9)));
            if (i % 3 == 0) {
                // Cuota del mes anterior, ya vencida y sin pagar.
                LocalDate mesAnterior = mesActual.minusMonths(1);
                repositorioCuota.save(crearCuota(guardado, mesAnterior, mesAnterior.plusDays(9)));
            }
        }
        log.info("Datos de demostración creados: {} familias ({} personas).", cantidadFamilias, cantidadFamilias * 3);
    }

    private Persona crearPersona(String nombre, String apellido, long dni, Parentesco parentesco, int anioNacimiento) {
        Persona persona = new Persona();
        persona.setNombre(nombre);
        persona.setApellido(apellido);
        persona.setDni(String.valueOf(dni));
        persona.setParentesco(parentesco);
        persona.setFechaNacimiento(LocalDate.of(anioNacimiento, 6, 15));
        persona.setEmail(nombre.toLowerCase().replace("í", "i").replace("á", "a").replace("é", "e").replace("ú", "u")
                .replace("ó", "o") + "." + dni + "@demo.club");
        return persona;
    }

    private Cuota crearCuota(GrupoFamiliar grupo, LocalDate periodo, LocalDate vencimiento) {
        Cuota cuota = new Cuota();
        cuota.setGrupoFamiliar(grupo);
        cuota.setPeriodo(periodo);
        cuota.setImporte(importeCuota);
        cuota.setFechaVencimiento(vencimiento);
        cuota.setEstado(EstadoCuota.PENDIENTE);
        return cuota;
    }
}
