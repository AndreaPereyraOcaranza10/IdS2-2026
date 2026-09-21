package com.club.fitnessclub.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de la foto de rostro. Se usa solo al subir o descargar la imagen.
 *
 * <p>Contiene bytes y metadatos, NO un MultipartFile: así la capa de servicio no depende de la
 * capa web. El controlador es quien extrae los datos del MultipartFile y arma este DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenRostroDTO {

    private String nombreArchivo;

    /** MIME type: image/jpeg o image/png. */
    private String contentType;

    private byte[] datos;
}
