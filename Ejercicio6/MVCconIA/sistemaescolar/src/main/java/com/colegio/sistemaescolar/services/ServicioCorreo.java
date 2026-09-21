package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.config.PropiedadesColegio;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.util.Map;

/**
 * SERVICIO de Correo saliente (infraestructura, no de negocio).
 *
 * <h2>Responsabilidad</h2>
 * Armar y enviar correos HTML. Es la única clase que conoce {@link JavaMailSender} (SMTP); el resto del
 * sistema solo publica eventos (ver {@code events/}) y el listener {@code EscuchaNotificaciones} llama a este servicio.
 *
 * <h2>Anotaciones</h2>
 * <ul>
 *   <li>{@code @Service}: bean de la capa de servicios, detectado por el escaneo de componentes.</li>
 *   <li>{@code @Slf4j} (Lombok): genera el logger {@code log}.</li>
 *   <li>{@code @Value}: inyecta una propiedad de application.properties (acá, el usuario SMTP).</li>
 * </ul>
 *
 * <h2>Modo simulado</h2>
 * Si {@code colegio.correo.habilitado=false} o no hay usuario SMTP configurado ({@code MAIL_USUARIO} vacío),
 * el correo NO se envía: se imprime en el log. Así el sistema es probable sin credenciales reales.
 *
 * <h2>Plantillas</h2>
 * El cuerpo se genera con Thymeleaf (carpeta {@code templates/correos/}), igual que las páginas web,
 * para que el diseño del correo sea HTML editable y no texto pegado en código Java.
 */
@Service
@Slf4j
public class ServicioCorreo {

    private final JavaMailSender enviador;
    private final SpringTemplateEngine motorPlantillas;
    private final PropiedadesColegio propiedades;
    private final String usuarioSmtp;

    public ServicioCorreo(JavaMailSender enviador,
                          SpringTemplateEngine motorPlantillas,
                          PropiedadesColegio propiedades,
                          @Value("${spring.mail.username:}") String usuarioSmtp) {
        this.enviador = enviador;
        this.motorPlantillas = motorPlantillas;
        this.propiedades = propiedades;
        this.usuarioSmtp = usuarioSmtp;
    }

    /**
     * Correo de bienvenida al docente recién registrado (requisito del enunciado).
     *
     * @param destinatario correo personal del docente
     * @param nombreCompleto "Nombre Apellido" para el saludo
     * @param pendienteAprobacion true si el ADMIN todavía debe habilitar la cuenta
     */
    public void enviarBienvenidaDocente(String destinatario, String nombreCompleto, boolean pendienteAprobacion) {
        Map<String, Object> variables = Map.of(
                "nombre", nombreCompleto,
                "correo", destinatario,
                "pendiente", pendienteAprobacion,
                "colegio", propiedades.getNombre(),
                "urlIngreso", propiedades.getUrlBase() + "/login",
                "anio", LocalDate.now().getYear());
        enviar(destinatario, "¡Bienvenido/a a " + propiedades.getNombre() + "!",
                "correos/bienvenida-docente", variables);
    }

    /** Aviso de seguridad: la contraseña fue modificada (si no fue el docente, debe avisar de inmediato). */
    public void enviarAvisoCambioPassword(String destinatario, String nombreCompleto) {
        Map<String, Object> variables = Map.of(
                "nombre", nombreCompleto,
                "colegio", propiedades.getNombre(),
                "contacto", propiedades.getEmailContacto(),
                "anio", LocalDate.now().getYear());
        enviar(destinatario, "Tu contraseña fue modificada", "correos/cambio-password", variables);
    }

    /**
     * Renderiza la plantilla y envía el mensaje. Nunca propaga excepciones: un fallo de SMTP no debe
     * deshacer un registro ya confirmado; se deja registrado en el log.
     */
    private void enviar(String destinatario, String asunto, String plantilla, Map<String, Object> variables) {
        try {
            Context contexto = new Context();
            contexto.setVariables(variables);
            String html = motorPlantillas.process(plantilla, contexto);

            if (!propiedades.getCorreo().isHabilitado() || usuarioSmtp == null || usuarioSmtp.isBlank()) {
                log.info("[CORREO SIMULADO] Para: {} | Asunto: {}\n{}", destinatario, asunto, html);
                return;
            }

            MimeMessage mensaje = enviador.createMimeMessage();
            MimeMessageHelper ayudante = new MimeMessageHelper(mensaje, "UTF-8");
            String remitente = propiedades.getCorreo().getRemitente();
            ayudante.setFrom(remitente == null || remitente.isBlank() ? usuarioSmtp : remitente);
            ayudante.setTo(destinatario);
            ayudante.setSubject(asunto);
            ayudante.setText(html, true);   // true = el contenido es HTML
            enviador.send(mensaje);
            log.info("Correo enviado a {} ({})", destinatario, asunto);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo '{}' a {}: {}", asunto, destinatario, e.getMessage());
        }
    }
}
