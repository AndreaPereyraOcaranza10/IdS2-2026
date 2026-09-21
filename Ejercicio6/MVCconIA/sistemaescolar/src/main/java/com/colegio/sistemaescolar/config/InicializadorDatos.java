package com.colegio.sistemaescolar.config;

import com.colegio.sistemaescolar.entities.Alumno;
import com.colegio.sistemaescolar.entities.AsignacionDocente;
import com.colegio.sistemaescolar.entities.Aula;
import com.colegio.sistemaescolar.entities.Docente;
import com.colegio.sistemaescolar.entities.Grado;
import com.colegio.sistemaescolar.entities.Materia;
import com.colegio.sistemaescolar.entities.Nota;
import com.colegio.sistemaescolar.entities.Usuario;
import com.colegio.sistemaescolar.enums.Periodo;
import com.colegio.sistemaescolar.enums.Rol;
import com.colegio.sistemaescolar.enums.Sexo;
import com.colegio.sistemaescolar.repositories.RepositorioAlumno;
import com.colegio.sistemaescolar.repositories.RepositorioAsignacionDocente;
import com.colegio.sistemaescolar.repositories.RepositorioAula;
import com.colegio.sistemaescolar.repositories.RepositorioDocente;
import com.colegio.sistemaescolar.repositories.RepositorioGrado;
import com.colegio.sistemaescolar.repositories.RepositorioMateria;
import com.colegio.sistemaescolar.repositories.RepositorioNota;
import com.colegio.sistemaescolar.repositories.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * INICIALIZADOR de datos: se ejecuta una vez al arrancar la aplicación ({@link ApplicationRunner}).
 *
 * <h2>Qué hace</h2>
 * <ol>
 *   <li>Crea el usuario ADMIN inicial si no existe (credenciales en {@code colegio.admin.*}). Sin él nadie
 *       podría entrar la primera vez, porque los docentes solo pueden autoregistrarse con rol DOCENTE.</li>
 *   <li>Si {@code colegio.datos-demo.habilitado=true} y la base está vacía, carga grados, aulas, materias, docentes,
 *       alumnos y notas de ejemplo para poder probar todo el sistema sin cargar datos a mano.</li>
 * </ol>
 * Es idempotente: si se reinicia la aplicación no duplica nada.
 *
 * <h2>Detalles</h2>
 * Usa repositorios directamente (no servicios) porque corre sin usuario autenticado y las reglas de autorización
 * de los servicios lo bloquearían. La auditoría registra "sistema" como autor de estas filas.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InicializadorDatos implements ApplicationRunner {

    private final PropiedadesColegio propiedades;
    private final PasswordEncoder codificador;
    private final Clock reloj;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioGrado repositorioGrado;
    private final RepositorioAula repositorioAula;
    private final RepositorioMateria repositorioMateria;
    private final RepositorioDocente repositorioDocente;
    private final RepositorioAlumno repositorioAlumno;
    private final RepositorioAsignacionDocente repositorioAsignacion;
    private final RepositorioNota repositorioNota;

    @Override
    @Transactional
    public void run(ApplicationArguments argumentos) {
        crearAdministrador();
        if (propiedades.getDatosDemo().isHabilitado() && repositorioGrado.countByEliminadoFalse() == 0) {
            cargarDatosDemo();
        }
    }

    private void crearAdministrador() {
        String email = propiedades.getAdmin().getEmail().trim().toLowerCase();
        if (repositorioUsuario.existsByEmailIgnoreCase(email)) {
            return;
        }
        Usuario admin = nuevoUsuario(email, propiedades.getAdmin().getPassword(), Rol.ADMIN);
        repositorioUsuario.save(admin);
        log.info("Usuario administrador creado: {}", email);
    }

    private void cargarDatosDemo() {
        // Grados y aulas
        Grado[] grados = new Grado[3];
        String[] nombresGrado = {"1.º Grado", "2.º Grado", "3.º Grado"};
        Aula[] aulas = new Aula[3];
        String[] codigosAula = {"A-101", "A-102", "B-201"};
        for (int i = 0; i < 3; i++) {
            Grado g = new Grado();
            g.setNombre(nombresGrado[i]);
            g.setNivel(i + 1);
            grados[i] = repositorioGrado.save(g);

            Aula a = new Aula();
            a.setCodigo(codigosAula[i]);
            a.setCapacidad(30);
            a.setUbicacion(i < 2 ? "Planta baja" : "Primer piso");
            aulas[i] = repositorioAula.save(a);
        }

        // Materias
        String[][] datosMaterias = {
                {"Matemática", "Aritmética, geometría y resolución de problemas."},
                {"Lengua", "Lectura, escritura y comprensión de textos."},
                {"Ciencias Naturales", "Seres vivos, materia y energía."},
                {"Ciencias Sociales", "Historia, geografía y ciudadanía."}};
        Materia[] materias = new Materia[datosMaterias.length];
        for (int i = 0; i < datosMaterias.length; i++) {
            Materia m = new Materia();
            m.setNombre(datosMaterias[i][0]);
            m.setDescripcion(datosMaterias[i][1]);
            materias[i] = repositorioMateria.save(m);
        }

        // Docentes (usuario = correo personal; contraseña común de demostración)
        // Asignaciones exactas (docente + grado + materia). Ana dicta Ciencias Naturales solo en 1.º; Carlos dicta
        // Ciencias Sociales solo en 3.º: sirve para probar que la autorización es por combinación y no por listas sueltas.
        Docente ana = repositorioDocente.save(
                nuevoDocente("Ana", "Ramírez", Sexo.FEMENINO, LocalDate.of(1985, 4, 12), "ana.ramirez@demo.edu.ar"));
        asignar(ana, grados[0], materias[0]);   // Matemática en 1.º
        asignar(ana, grados[1], materias[0]);   // Matemática en 2.º
        asignar(ana, grados[0], materias[2]);   // Ciencias Naturales en 1.º

        Docente carlos = repositorioDocente.save(
                nuevoDocente("Carlos", "Benítez", Sexo.MASCULINO, LocalDate.of(1979, 9, 3), "carlos.benitez@demo.edu.ar"));
        asignar(carlos, grados[1], materias[1]);   // Lengua en 2.º
        asignar(carlos, grados[2], materias[1]);   // Lengua en 3.º
        asignar(carlos, grados[2], materias[3]);   // Ciencias Sociales en 3.º

        // Alumnos: 4 por grado
        String[][] alumnos = {
                {"51234001", "Lucía", "Fernández"}, {"51234002", "Mateo", "González"},
                {"51234003", "Valentina", "Rodríguez"}, {"51234004", "Santiago", "López"},
                {"52234001", "Camila", "Martínez"}, {"52234002", "Joaquín", "Pérez"},
                {"52234003", "Sofía", "Sánchez"}, {"52234004", "Tomás", "Romero"},
                {"53234001", "Martina", "Torres"}, {"53234002", "Benjamín", "Díaz"},
                {"53234003", "Emma", "Álvarez"}, {"53234004", "Lautaro", "Ruiz"}};
        int indice = 0;
        for (String[] datos : alumnos) {
            int g = indice / 4;
            Alumno alumno = new Alumno();
            alumno.setDni(datos[0]);
            alumno.setNombre(datos[1]);
            alumno.setApellido(datos[2]);
            alumno.setSexo(indice % 2 == 0 ? Sexo.FEMENINO : Sexo.MASCULINO);
            alumno.setFechaNacimiento(LocalDate.now(reloj).minusYears(6L + g).minusDays(30L * (indice % 4 + 1)));
            alumno.setDomicilio("Calle Demo " + (100 + indice) + ", Mendoza");
            alumno.setNombreTutor("Tutor de " + datos[2]);
            alumno.setTelefonoTutor("261555" + (1000 + indice));
            alumno.setGrado(grados[g]);
            alumno.setAula(aulas[g]);
            alumno = repositorioAlumno.save(alumno);

            // Notas de ejemplo: cada alumno tiene una nota por materia y trimestre (valores determinísticos).
            for (int m = 0; m < materias.length; m++) {
                for (int p = 0; p < 2; p++) {
                    Nota nota = new Nota();
                    nota.setAlumno(alumno);
                    nota.setMateria(materias[m]);
                    nota.setPeriodo(p == 0 ? Periodo.PRIMER_TRIMESTRE : Periodo.SEGUNDO_TRIMESTRE);
                    nota.setValor(BigDecimal.valueOf(4 + ((indice * 3 + m * 2 + p * 5) % 6)).add(new BigDecimal("0.50")));
                    nota.setFecha(LocalDate.now(reloj).minusDays(20L * (2 - p)));
                    nota.setObservaciones(null);
                    repositorioNota.save(nota);
                }
            }
            indice++;
        }
        log.info("Datos de demostración cargados. Docentes: ana.ramirez@demo.edu.ar y carlos.benitez@demo.edu.ar");
    }

    private void asignar(Docente docente, Grado grado, Materia materia) {
        AsignacionDocente asignacion = new AsignacionDocente();
        asignacion.setDocente(docente);
        asignacion.setGrado(grado);
        asignacion.setMateria(materia);
        repositorioAsignacion.save(asignacion);
    }

    private Docente nuevoDocente(String nombre, String apellido, Sexo sexo, LocalDate nacimiento, String email) {
        Usuario usuario = repositorioUsuario.save(
                nuevoUsuario(email, propiedades.getDatosDemo().getPasswordDocentes(), Rol.DOCENTE));
        Docente docente = new Docente();
        docente.setNombre(nombre);
        docente.setApellido(apellido);
        docente.setSexo(sexo);
        docente.setFechaNacimiento(nacimiento);
        docente.setUsuario(usuario);
        return docente;
    }

    private Usuario nuevoUsuario(String email, String password, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPasswordHash(codificador.encode(password));
        usuario.setRol(rol);
        usuario.setHabilitado(true);
        usuario.setUltimoCambioPassword(LocalDateTime.now(reloj));
        return usuario;
    }
}
