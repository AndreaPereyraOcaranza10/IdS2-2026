package com.club.fitnessclub.services;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.club.fitnessclub.dtos.ImagenRostroDTO;
import com.club.fitnessclub.dtos.PersonaDTO;
import com.club.fitnessclub.entities.GrupoFamiliar;
import com.club.fitnessclub.entities.ImagenRostro;
import com.club.fitnessclub.entities.Persona;
import com.club.fitnessclub.enums.Parentesco;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.mappers.MapeadorPersona;
import com.club.fitnessclub.repositories.RepositorioGrupoFamiliar;
import com.club.fitnessclub.repositories.RepositorioPersona;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de negocio de Persona (socios y familiares) y de su foto de rostro.
 *
 * <p>REGLAS DE NEGOCIO que garantiza:
 * <ol>
 *   <li>El DNI es único (incluso frente a personas dadas de baja).</li>
 *   <li>La familia debe existir y estar vigente.</li>
 *   <li>El PRIMER integrante de una familia debe ser el TITULAR; no puede haber un segundo titular.</li>
 *   <li>En modificaciones no se cambian familia ni parentesco.</li>
 *   <li>No se da de baja al titular mientras queden otros integrantes vigentes.</li>
 *   <li>La foto debe ser JPEG o PNG, de hasta 3 MB, y su contenido debe corresponder al formato declarado.</li>
 *   <li>La foto es OBLIGATORIA en el alta desde el formulario ({@link #save(PersonaDTO, ImagenRostroDTO)}).</li>
 * </ol>
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ServicioPersona implements ServicioBase<PersonaDTO> {

    /** Tamaño máximo de la foto: 3 MB (coincide con spring.servlet.multipart.max-file-size). */
    private static final long TAMANIO_MAXIMO_IMAGEN = 3L * 1024 * 1024;

    private static final Set<String> TIPOS_IMAGEN_PERMITIDOS = Set.of("image/jpeg", "image/png");

    private final RepositorioPersona repositorioPersona;
    private final RepositorioGrupoFamiliar repositorioGrupo;
    private final MapeadorPersona mapeador;

    // ------------------------------------------------------------------ CONSULTAS

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<PersonaDTO> findAll() throws Exception {
        return repositorioPersona.findByActivoTrueOrderByApellidoAscNombreAsc().stream()
                .map(mapeador::aDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PersonaDTO findById(long id) throws Exception {
        return mapeador.aDTO(buscarActiva(id));
    }

    /** Integrantes vigentes de una familia (para la pantalla de detalle de familia). */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<PersonaDTO> findByGrupoFamiliar(long grupoFamiliarId) throws Exception {
        return repositorioPersona
                .findByGrupoFamiliarIdAndActivoTrueOrderByApellidoAscNombreAsc(grupoFamiliarId).stream()
                .map(mapeador::aDTO)
                .toList();
    }

    /** Búsqueda por DNI. */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PersonaDTO findByDni(String dni) throws Exception {
        return repositorioPersona.findByDniAndActivoTrue(dni)
                .map(mapeador::aDTO)
                .orElseThrow(() -> new ExcepcionNegocio("No se encontró una persona vigente con DNI " + dni + "."));
    }

    // ------------------------------------------------------------- ALTA / MODIF. / BAJA

    /**
     * Alta SIN foto (contrato de ServicioBase). Se usa para datos de demostración y pruebas; el
     * formulario web usa {@link #save(PersonaDTO, ImagenRostroDTO)}, que exige la foto.
     */
    @Override
    public PersonaDTO save(PersonaDTO dto) throws Exception {
        // Regla 2: la familia existe y está vigente, o se crea una nueva.
        GrupoFamiliar grupo = resolverFamilia(dto);

        // Regla 1: DNI único.
        if (repositorioPersona.existsByDni(dto.getDni().trim())) {
            throw new ExcepcionNegocio("Ya existe una persona registrada con el DNI " + dto.getDni()
                    + " (puede estar dada de baja).");
        }

        // Regla 3: titular único y primero.
        boolean tieneTitular = repositorioPersona
                .existsByGrupoFamiliarIdAndParentescoAndActivoTrue(grupo.getId(), Parentesco.TITULAR);
        if (tieneTitular && dto.getParentesco() == Parentesco.TITULAR) {
            throw new ExcepcionNegocio("La familia ya tiene un titular.");
        }
        if (!tieneTitular && dto.getParentesco() != Parentesco.TITULAR) {
            throw new ExcepcionNegocio("El primer integrante de la familia debe ser el titular.");
        }

        Persona persona = mapeador.aEntidad(dto);
        // agregarIntegrante sincroniza ambos lados de la relación (lista y FK).
        grupo.agregarIntegrante(persona);
        return mapeador.aDTO(repositorioPersona.save(persona));
    }

    /**
     * Alta CON foto: valida la imagen ANTES de tocar la base y guarda persona + foto en la MISMA
     * transacción (si algo falla, no queda una persona sin foto a medio guardar). Regla 7.
     */
    public PersonaDTO save(PersonaDTO dto, ImagenRostroDTO imagen) throws Exception {
        if (imagen == null) {
            throw new ExcepcionNegocio("La foto de rostro es obligatoria.");
        }
        validarImagen(imagen);

        PersonaDTO creada = save(dto);
        guardarRostro(creada.getId(), imagen);
        return findById(creada.getId());
    }

    @Override
    public PersonaDTO update(long id, PersonaDTO dto) throws Exception {
        Persona persona = buscarActiva(id);

        // Regla 1 (modificación): el DNI no puede pertenecer a OTRA persona.
        if (repositorioPersona.existsByDniAndIdNot(dto.getDni().trim(), id)) {
            throw new ExcepcionNegocio("Ya existe otra persona registrada con el DNI " + dto.getDni() + ".");
        }

        // Regla 4: solo se actualizan datos personales (familia y parentesco se conservan).
        mapeador.actualizarEntidad(persona, dto);
        return mapeador.aDTO(repositorioPersona.save(persona));
    }

    /** Modificación con foto opcional: si viene una imagen, reemplaza a la anterior (misma transacción). */
    public PersonaDTO update(long id, PersonaDTO dto, ImagenRostroDTO imagen) throws Exception {
        if (imagen != null) {
            validarImagen(imagen);
        }
        PersonaDTO actualizada = update(id, dto);
        if (imagen == null) {
            return actualizada;
        }
        guardarRostro(id, imagen);
        return findById(id);
    }

    /** BAJA LÓGICA (regla 5). */
    @Override
    public void delete(long id) throws Exception {
        Persona persona = buscarActiva(id);

        if (persona.getParentesco() == Parentesco.TITULAR
                && repositorioPersona.countByGrupoFamiliarIdAndActivoTrue(persona.getGrupoFamiliar().getId()) > 1) {
            throw new ExcepcionNegocio("No se puede dar de baja al titular mientras la familia tenga otros integrantes. "
                    + "Dé de baja primero a los demás integrantes o a la familia completa.");
        }

        persona.setActivo(false);
        repositorioPersona.save(persona);
    }

    // ------------------------------------------------------------------ FOTO DE ROSTRO

    /**
     * Guarda (o reemplaza) la foto de rostro de una persona.
     * Si ya tenía foto se reutiliza la misma fila de imagen_rostro (se actualizan sus campos);
     * si no, se crea una nueva y el cascade ALL de Persona la persiste.
     */
    public void guardarRostro(long personaId, ImagenRostroDTO imagen) throws Exception {
        validarImagen(imagen);

        Persona persona = buscarActiva(personaId);
        ImagenRostro entidad = persona.getImagenRostro();
        if (entidad == null) {
            entidad = new ImagenRostro();
            persona.setImagenRostro(entidad);
        }
        entidad.setNombreArchivo(imagen.getNombreArchivo());
        entidad.setContentType(imagen.getContentType());
        entidad.setDatos(imagen.getDatos());

        repositorioPersona.save(persona);
    }

    /**
     * Devuelve la foto para que el controlador la sirva como imagen.
     * Es el ÚNICO punto donde se leen los bytes (la relación es LAZY).
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ImagenRostroDTO obtenerRostro(long personaId) throws Exception {
        Persona persona = buscarActiva(personaId);
        ImagenRostro entidad = persona.getImagenRostro();
        if (entidad == null) {
            throw new ExcepcionNegocio("La persona no tiene foto de rostro cargada.");
        }
        return new ImagenRostroDTO(entidad.getNombreArchivo(), entidad.getContentType(), entidad.getDatos());
    }

    // ------------------------------------------------------------------ AUXILIARES

    /** Busca una persona vigente o lanza ExcepcionNegocio. */
    private Persona buscarActiva(long id) throws ExcepcionNegocio {
        return repositorioPersona.findById(id)
                .filter(Persona::isActivo)
                .orElseThrow(() -> new ExcepcionNegocio("La persona solicitada no existe o fue dada de baja."));
    }

    /**
     * Regla 6: valida tamaño, tipo declarado y "firma" real del archivo. El content-type lo
     * informa el cliente y puede falsearse; por eso se comprueban también los primeros bytes
     * (magic numbers): JPEG = FF D8 FF, PNG = 89 50 4E 47.
     */
    private void validarImagen(ImagenRostroDTO imagen) throws ExcepcionNegocio {
        if (imagen == null || imagen.getDatos() == null || imagen.getDatos().length == 0) {
            throw new ExcepcionNegocio("Debe seleccionar una imagen.");
        }
        if (imagen.getDatos().length > TAMANIO_MAXIMO_IMAGEN) {
            throw new ExcepcionNegocio("La imagen no puede superar los 3 MB.");
        }
        if (!TIPOS_IMAGEN_PERMITIDOS.contains(imagen.getContentType())) {
            throw new ExcepcionNegocio("Formato no permitido: solo se aceptan imágenes JPEG o PNG.");
        }
        byte[] b = imagen.getDatos();
        boolean esJpeg = b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
        boolean esPng = b.length > 4 && (b[0] & 0xFF) == 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47;
        if (!esJpeg && !esPng) {
            throw new ExcepcionNegocio("El archivo no es una imagen JPEG o PNG válida.");
        }
    }

    /**
     * Devuelve la familia de la persona: la existente si se eligió una, o una NUEVA si se escribió
     * un nombre. Si se informan ambas, tiene prioridad la existente. Si falla algo después, la
     * transacción se revierte y la familia nueva no queda guardada.
     */
    private GrupoFamiliar resolverFamilia(PersonaDTO dto) throws ExcepcionNegocio {
        if (dto.getGrupoFamiliarId() != null) {
            return repositorioGrupo.findById(dto.getGrupoFamiliarId())
                    .filter(GrupoFamiliar::isActivo)
                    .orElseThrow(() -> new ExcepcionNegocio("La familia seleccionada no existe o fue dada de baja."));
        }
        String nombre = dto.getNuevaFamiliaNombre() == null ? "" : dto.getNuevaFamiliaNombre().trim();
        if (nombre.isEmpty()) {
            throw new ExcepcionNegocio("Seleccioná una familia existente o escribí el nombre de una familia nueva.");
        }
        GrupoFamiliar nueva = new GrupoFamiliar();
        nueva.setNombre(nombre);
        return repositorioGrupo.save(nueva);
    }
}
