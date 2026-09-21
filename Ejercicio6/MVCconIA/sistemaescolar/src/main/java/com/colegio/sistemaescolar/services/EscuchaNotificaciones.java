package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.events.DocenteRegistradoEvento;
import com.colegio.sistemaescolar.events.PasswordCambiadaEvento;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * LISTENER de eventos de dominio que dispara notificaciones por correo.
 *
 * <h2>Por qué eventos</h2>
 * {@code ServicioDocente} no sabe nada de SMTP: solo publica un {@link DocenteRegistradoEvento}. Este listener lo recibe
 * y manda el correo. Ventajas: (1) el servicio queda simple y testeable, (2) el envío no demora la respuesta HTTP
 * y (3) si el SMTP falla, el registro del docente NO se revierte.
 *
 * <h2>Anotaciones</h2>
 * <ul>
 *   <li>{@code @TransactionalEventListener(phase = AFTER_COMMIT)}: el método corre recién cuando la transacción
 *       del registro se confirmó. Así jamás se envía un "bienvenido" de un usuario que terminó revirtiéndose.</li>
 *   <li>{@code @Async}: se ejecuta en otro hilo (habilitado con {@code @EnableAsync} en ConfiguracionAplicacion).</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class EscuchaNotificaciones {

    private final ServicioCorreo servicioCorreo;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alRegistrarseDocente(DocenteRegistradoEvento evento) {
        servicioCorreo.enviarBienvenidaDocente(evento.email(), evento.nombreCompleto(), evento.pendienteAprobacion());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCambiarPassword(PasswordCambiadaEvento evento) {
        servicioCorreo.enviarAvisoCambioPassword(evento.email(), evento.nombreCompleto());
    }
}
