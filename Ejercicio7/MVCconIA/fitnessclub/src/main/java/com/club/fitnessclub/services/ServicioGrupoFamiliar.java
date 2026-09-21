package com.club.fitnessclub.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.club.fitnessclub.dtos.GrupoFamiliarDTO;
import com.club.fitnessclub.entities.GrupoFamiliar;
import com.club.fitnessclub.entities.Persona;
import com.club.fitnessclub.enums.EstadoCuota;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.mappers.MapeadorGrupoFamiliar;
import com.club.fitnessclub.repositories.RepositorioCuota;
import com.club.fitnessclub.repositories.RepositorioGrupoFamiliar;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de negocio de GrupoFamiliar (ABM de familias).
 *
 * <ul>
 *   <li>@Service: componente de la capa de servicio (lo detecta @ComponentScan).</li>
 *   <li>@Transactional(rollbackFor = Exception.class): cada método público corre en una
 *       transacción; si lanza CUALQUIER excepción (incluidas las checked como ExcepcionNegocio)
 *       se revierte todo.</li>
 *   <li>@RequiredArgsConstructor (Lombok): genera el constructor con los campos final, y Spring
 *       inyecta las dependencias por constructor (facilita los tests con Mockito).</li>
 * </ul>
 *
 * <p>Recibe y devuelve DTOs; las entidades no salen de esta clase.
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ServicioGrupoFamiliar implements ServicioBase<GrupoFamiliarDTO> {

    private final RepositorioGrupoFamiliar repositorioGrupo;
    private final RepositorioCuota repositorioCuota;
    private final MapeadorGrupoFamiliar mapeador;

    /** readOnly = true: optimización de Hibernate para consultas (no hace dirty checking). */
    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<GrupoFamiliarDTO> findAll() throws Exception {
        return repositorioGrupo.findAllActivosConIntegrantes().stream()
                .map(mapeador::aDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public GrupoFamiliarDTO findById(long id) throws Exception {
        return mapeador.aDTO(buscarActivo(id));
    }

    @Override
    public GrupoFamiliarDTO save(GrupoFamiliarDTO dto) throws Exception {
        GrupoFamiliar grupo = mapeador.aEntidad(dto);
        // Los campos de auditoría (creado_por, creado_en, ...) los completa Spring al persistir.
        return mapeador.aDTO(repositorioGrupo.save(grupo));
    }

    @Override
    public GrupoFamiliarDTO update(long id, GrupoFamiliarDTO dto) throws Exception {
        GrupoFamiliar grupo = buscarActivo(id);
        mapeador.actualizarEntidad(grupo, dto);
        // La entidad está "gestionada": los cambios se guardan al confirmar la transacción.
        return mapeador.aDTO(repositorioGrupo.save(grupo));
    }

    /**
     * BAJA LÓGICA de la familia. Reglas:
     * <ul>
     *   <li>No se puede dar de baja una familia con cuotas PENDIENTES (primero deben pagarse o anularse).</li>
     *   <li>Al dar de baja la familia también se dan de baja todos sus integrantes vigentes
     *       (no puede haber personas activas en una familia inactiva).</li>
     * </ul>
     */
    @Override
    public void delete(long id) throws Exception {
        GrupoFamiliar grupo = buscarActivo(id);

        if (repositorioCuota.existsByGrupoFamiliarIdAndEstadoAndActivoTrue(id, EstadoCuota.PENDIENTE)) {
            throw new ExcepcionNegocio("No se puede dar de baja la familia: tiene cuotas pendientes. "
                    + "Regístrelas como pagas o anúlelas primero.");
        }

        grupo.getIntegrantes().stream()
                .filter(Persona::isActivo)
                .forEach(p -> p.setActivo(false));
        grupo.setActivo(false);
        repositorioGrupo.save(grupo);
    }

    /** Busca una familia vigente o lanza ExcepcionNegocio. Reutilizado por los métodos de arriba. */
    private GrupoFamiliar buscarActivo(long id) throws ExcepcionNegocio {
        return repositorioGrupo.findById(id)
                .filter(GrupoFamiliar::isActivo)
                .orElseThrow(() -> new ExcepcionNegocio("La familia solicitada no existe o fue dada de baja."));
    }
}
