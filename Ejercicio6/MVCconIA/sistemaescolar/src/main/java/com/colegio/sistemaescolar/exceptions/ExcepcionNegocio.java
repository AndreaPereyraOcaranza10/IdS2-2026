package com.colegio.sistemaescolar.exceptions;

import org.springframework.validation.BindingResult;

/**
 * Error de REGLA DE NEGOCIO (por ejemplo "el correo ya está registrado").
 *
 * <p>Es una excepción <b>verificada</b> (extiende {@code Exception}), igual que en el resto de los
 * proyectos: obliga a quien la puede recibir a declararla o capturarla. Por eso los métodos de servicio
 * usan {@code @Transactional(rollbackFor = Exception.class)}: por defecto Spring solo revierte la
 * transacción con excepciones no verificadas.</p>
 *
 * <p>Si se indica un {@code campo}, el controlador asocia el mensaje a ese campo del formulario
 * (aparece debajo del input); si no, se muestra como error general.</p>
 */
public class ExcepcionNegocio extends Exception {

    private final String campo;

    public ExcepcionNegocio(String mensaje) {
        super(mensaje);
        this.campo = null;
    }

    public ExcepcionNegocio(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }

    /** Vuelca este error en el {@code BindingResult} para que la vista lo muestre. */
    public void aplicarEn(BindingResult resultado) {
        if (campo != null) {
            resultado.rejectValue(campo, "negocio", getMessage());
        } else {
            resultado.reject("negocio", getMessage());
        }
    }
}
