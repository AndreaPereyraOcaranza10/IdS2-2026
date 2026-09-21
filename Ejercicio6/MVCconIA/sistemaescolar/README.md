# Sistema de Gestión Escolar (Ingeniería de Software 2)

Sistema web para registrar **alumnos** (con su **grado** y **aula**), sus **docentes** y las **notas por materia**.

## Tecnologías
| Capa | Tecnología |
|---|---|
| Lenguaje / Framework | Java 17, Spring Boot 3.3.5 |
| Vista (MVC) | Thymeleaf + thymeleaf-extras-springsecurity6 |
| Persistencia (ORM) | Spring Data JPA / Hibernate + MySQL |
| Seguridad | Spring Security (formulario de login, BCrypt, roles ADMIN y DOCENTE) |
| Validación | Jakarta Bean Validation |
| Correo | Spring Mail (SMTP) con plantillas Thymeleaf y envío asíncrono |
| Plantillas HTML/CSS | **Oinia** (Bootstrap 4) para el sitio público, login y registro + **SB Admin 7** (Bootstrap 5) para el panel |
| Tests | JUnit 5, Mockito, AssertJ |

## Arquitectura (MVC por capas)
`Controller → Service → Mapper → Repository → Entity`, con **DTO** entre capas (las entidades nunca llegan a la vista).
- `entities/` entidades JPA. `Auditable` da id, baja lógica y auditoría (quién/cuándo creó y modificó).
- `dtos/` objetos de transferencia con validaciones. `mappers/` conversión Entity ↔ DTO.
- `services/` reglas de negocio y transacciones. `ServicioAcceso` limita los datos por docente.
- `controllers/` rutas web. `security/` y `config/` seguridad, auditoría, propiedades y datos iniciales.
- `events/` + `EscuchaNotificaciones`: eventos de dominio que disparan los correos.

## Cómo ejecutar
1. Tener **JDK 17+**, **Maven** y **MySQL** (XAMPP: iniciar el módulo MySQL).
2. Ajustar si hace falta `DB_USUARIO` / `DB_CLAVE` (por defecto `root` sin clave). La base `colegio` se crea sola.
3. `mvn clean package` y luego `mvn spring-boot:run` (o `mvn test` para los tests).
4. Abrir http://localhost:8080

### Usuarios de prueba (datos demo)
| Rol | Usuario | Contraseña |
|---|---|---|
| ADMIN | admin@colegio.local | Admin1234 |
| DOCENTE | ana.ramirez@demo.edu.ar | Docente1234 |
| DOCENTE | carlos.benitez@demo.edu.ar | Docente1234 |

Para arrancar sin datos de ejemplo: `colegio.datos-demo.habilitado=false`.

## Funcionalidades
- **Sitio público:** inicio, grados, servicios, contacto, login y **registro de docentes**.
- **Registro de docente:** Nombre, Apellido, Sexo, Fecha de nacimiento, correo personal (= **usuario**) y contraseña.
  Se crea el usuario con contraseña cifrada (BCrypt) y se envía un **correo de bienvenida** al correo personal.
- **Cambio de contraseña:** en *Mi perfil* (pide la actual; avisa por correo).
- **ADMIN:** ABM de alumnos, docentes (habilita/deshabilita y asigna *materia por grado* en una matriz), grados, aulas (con cupo) y materias.
- **DOCENTE:** ve solo los alumnos de los grados donde tiene asignaciones y carga/edita notas únicamente en las materias que dicta **en el grado de ese alumno**.
- **Asignaciones:** entidad `AsignacionDocente` (docente + grado + materia, única por terna). Ej.: la docente demo Ana dicta Ciencias Naturales solo en 1.º Grado.
- **Notas y boletín:** escala 1–10, períodos (3 trimestres + final), promedios por materia y general, aprobación con 6.

## Correo
Por defecto funciona en **modo simulado** (el correo se imprime en la consola). Para enviar de verdad:
`MAIL_USUARIO`, `MAIL_CLAVE` (contraseña de aplicación en Gmail) y opcionalmente `MAIL_HOST`, `MAIL_PUERTO`, `MAIL_REMITENTE`, `URL_BASE`.

## Registro con aprobación
`colegio.registro.requiere-aprobacion=false` (por defecto): el docente ingresa apenas se registra, como pide el enunciado.
Con `true`, queda deshabilitado hasta que el ADMIN lo habilite.

## Sobre las plantillas
`admincn` (Next.js/React/shadcn) no es compatible con Thymeleaf y no se usó. Se adaptó **Oinia** para la parte pública y **SB Admin** para el panel
(assets locales en `static/recursos`, sin CDN). Se retiró la marca "Oinia".
