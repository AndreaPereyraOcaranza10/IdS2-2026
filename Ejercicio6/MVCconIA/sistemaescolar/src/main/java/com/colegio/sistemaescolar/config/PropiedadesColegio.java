package com.colegio.sistemaescolar.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades propias del sistema, leídas de {@code application.properties} (prefijo {@code colegio}).
 *
 * <p>{@code @ConfigurationProperties} enlaza cada clave con un atributo: por ejemplo
 * {@code colegio.registro.requiere-aprobacion} -> {@code getRegistro().isRequiereAprobacion()}
 * (Spring convierte los guiones de "kebab-case" a camelCase). Tener las propiedades tipadas en
 * una clase evita repetir {@code @Value("${...}")} por todo el código.</p>
 */
@ConfigurationProperties(prefix = "colegio")
@Getter
@Setter
public class PropiedadesColegio {

    /** Nombre institucional que se muestra en las vistas y en los correos. */
    private String nombre = "Colegio Horizonte";

    /** URL pública de la aplicación (se usa para armar el enlace de ingreso de los correos). */
    private String urlBase = "http://localhost:8080";

    private String direccion = "";
    private String telefono = "";
    private String emailContacto = "";

    private Correo correo = new Correo();
    private Registro registro = new Registro();
    private Admin admin = new Admin();
    private DatosDemo datosDemo = new DatosDemo();

    /** Configuración del envío de correos. */
    @Getter
    @Setter
    public static class Correo {
        /** Si es false, nunca se intenta enviar por SMTP (solo se registra en el log). */
        private boolean habilitado = true;
        /** Dirección remitente. Si está vacía se usa {@code spring.mail.username}. */
        private String remitente = "";
    }

    /** Política del registro público de docentes. */
    @Getter
    @Setter
    public static class Registro {
        /** true = la cuenta nueva queda deshabilitada hasta que un ADMIN la apruebe. */
        private boolean requiereAprobacion = false;
    }

    /** Credenciales del administrador inicial. */
    @Getter
    @Setter
    public static class Admin {
        private String email = "admin@colegio.local";
        private String password = "Admin1234";
    }

    /** Datos de demostración cargados al primer arranque. */
    @Getter
    @Setter
    public static class DatosDemo {
        private boolean habilitado = true;
        private String passwordDocentes = "Docente1234";
    }
}
