package com.techstore.inventario.dto;

import com.techstore.inventario.entities.RolUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada: datos del formulario de alta/edicion de Usuario.
 * En edicion, password puede venir vacio (se conserva el actual); en alta es obligatorio.
 * La validacion condicional de password (obligatorio solo en alta) se hace a mano
 * en el controller, porque @NotBlank no puede depender de si id es null o no.
 */
public class UsuarioFormDTO {

    private Long id; // null en alta

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    private String password; // ver nota de clase sobre validacion condicional

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "Debe seleccionar un rol")
    private RolUsuario rol;

    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
