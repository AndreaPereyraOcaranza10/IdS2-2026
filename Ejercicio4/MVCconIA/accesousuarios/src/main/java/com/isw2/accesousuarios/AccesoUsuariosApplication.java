package com.isw2.accesousuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PUNTO DE ENTRADA DE LA APLICACIÓN
 * ============================================================================
 * SISTEMA DE REGISTRO E INGRESO DE USUARIOS  -  Ingeniería de Software 2
 *
 * ENUNCIADO
 *   Las personas que interactúan con el sistema tienen un usuario y clave.
 *   Se registran con Nombre, Apellido, Documento, Fecha de Nacimiento y Correo
 *   Personal. Al ingresar, si el usuario no está registrado se le solicita que
 *   se registre. El usuario del sistema es el correo personal. Si el usuario
 *   está registrado y se equivoca 3 veces con la clave, se bloquea.
 *
 * FUNCIONALIDADES IMPLEMENTADAS
 *   1) Registro de una persona (GET/POST /registro).
 *   2) Ingreso al sistema con correo + clave (GET/POST /login).
 *   3) Si el correo NO está registrado -> se redirige al formulario de registro
 *      con el correo ya cargado y un aviso.
 *   4) Si la clave es incorrecta -> se cuenta el intento fallido; al 3er error
 *      consecutivo la cuenta queda BLOQUEADA y ya no se puede ingresar.
 *   5) Pantalla de inicio (GET /inicio) visible solo con sesión iniciada, y
 *      cierre de sesión (POST /logout).
 *
 * ARQUITECTURA MVC EN CAPAS (flujo de una petición)
 *
 *   Navegador
 *      |  HTTP
 *      v
 *   [ VISTA ]        templates/*.html (Thymeleaf + Bootstrap). Dibuja los
 *      ^  |          formularios y muestra errores. Solo conoce DTOs.
 *      |  v
 *   [ CONTROLADOR ]  controllers/  Recibe la petición, valida el formato del
 *      ^  |          formulario (@Valid), llama al servicio y elige la vista
 *      |  v          o la redirección. NO contiene reglas de negocio.
 *   [ SERVICIO ]     services/  Reglas de negocio: unicidad de correo y
 *      ^  |          documento, hash de la clave, conteo de intentos, bloqueo.
 *      |  v          Maneja las transacciones.
 *   [ REPOSITORIO ]  repositories/  Acceso a datos (Spring Data JPA).
 *      ^  |
 *      |  v
 *   [ MODELO ]       entities/  Entidades JPA (Persona, Usuario) mapeadas por
 *      ^  |          el ORM (Hibernate) a tablas de MySQL.
 *      |  v
 *   [ MySQL ]
 *
 *   Elementos transversales:
 *     - dtos/        Objetos que viajan entre controlador y vista/servicio.
 *                    Las ENTIDADES nunca llegan a la vista (limite de la arquitectura).
 *     - mappers/     Convierten DTO <-> Entidad.
 *     - exceptions/  Excepción de negocio con el motivo del rechazo.
 *
 * ANOTACIÓN @SpringBootApplication (meta-anotación que agrupa tres):
 *   - @SpringBootConfiguration : esta clase declara configuración de Spring.
 *   - @EnableAutoConfiguration : Spring Boot configura solo el DataSource, JPA,
 *                                Thymeleaf, Tomcat, etc. segun las dependencias
 *                                del pom.xml y application.properties.
 *   - @ComponentScan           : escanea este paquete y sus subpaquetes buscando
 *                                @Controller, @Service, @Component, @Repository
 *                                y los registra como beans en el contenedor.
 *   Por eso esta clase debe estar en el paquete raíz (com.isw2.accesousuarios).
 */
@SpringBootApplication
public class AccesoUsuariosApplication {

    /** Arranca el contenedor de Spring y el servidor Tomcat embebido (puerto 8080). */
    public static void main(String[] args) {
        SpringApplication.run(AccesoUsuariosApplication.class, args);
    }
}
