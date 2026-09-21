package com.techstore.inventario.dto;

import jakarta.validation.constraints.*;

/** DTO de entrada: datos del formulario de alta/edicion de Proveedor. */
public class ProveedorFormDTO {

    private Long id; // null en alta

    @NotBlank(message = "La razon social es obligatoria")
    private String razonSocial;

    @NotBlank(message = "El CUIT es obligatorio")
    private String cuit;

    private String telefono;

    @Email(message = "El email no tiene un formato valido")
    private String email;

    private String direccion;

    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getCuit() { return cuit; }
    public void setCuit(String cuit) { this.cuit = cuit; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
