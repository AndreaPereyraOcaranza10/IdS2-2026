package com.club.fitnessclub.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Imagen del rostro de una persona, almacenada en la base de datos.
 *
 * <p>Por qué una entidad separada: si los bytes estuvieran en Persona, cada
 * listado los cargaría. Aislada y referenciada LAZY, la imagen solo se lee
 * cuando se pide (endpoint que la sirve).
 *
 * <p>Límite de tamaño: multipart (3MB) en application.properties, y el servicio
 * valida además el content-type (solo image/jpeg y image/png) y la firma real del archivo.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "imagen_rostro")
public class ImagenRostro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** Nombre original del archivo subido. */
    @Column(name = "nombre_archivo", length = 150)
    private String nombreArchivo;

    /** MIME type (image/jpeg, image/png) para responder con el Content-Type correcto. */
    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    /**
     * Bytes de la imagen. @Lob = objeto grande; MEDIUMBLOB en MySQL admite hasta 16 MB
     * (el BLOB estándar solo 64 KB, insuficiente para una foto).
     */
    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] datos;
}
