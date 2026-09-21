package com.club.fitnessclub.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.club.fitnessclub.dtos.CuotaDTO;
import com.club.fitnessclub.dtos.GeneracionCuotasDTO;
import com.club.fitnessclub.entities.Cuota;
import com.club.fitnessclub.entities.GrupoFamiliar;
import com.club.fitnessclub.enums.EstadoCuota;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.mappers.MapeadorCuota;
import com.club.fitnessclub.repositories.RepositorioCuota;
import com.club.fitnessclub.repositories.RepositorioGrupoFamiliar;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de CUOTAS del club (emisión, modificación y anulación). El cobro se registra en
 * {@link ServicioPago}.
 *
 * <p>REGLAS DE NEGOCIO:
 * <ol>
 *   <li>Una familia tiene como máximo UNA cuota por período (mes).</li>
 *   <li>La fecha de vencimiento no puede ser anterior al primer día del período.</li>
 *   <li>Solo se emiten cuotas a familias vigentes.</li>
 *   <li>Una cuota ANULADA no se puede modificar; el importe no puede quedar por debajo de lo ya pagado.</li>
 *   <li>"delete" = ANULAR: solo si la cuota no tiene pagos vigentes (primero se anulan los pagos).
 *       La cuota anulada se conserva con estado ANULADA para no perder el historial.</li>
 * </ol>
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ServicioCuota implements ServicioBase<CuotaDTO> {

    private final RepositorioCuota repositorioCuota;
    private final RepositorioGrupoFamiliar repositorioGrupo;
    private final MapeadorCuota mapeador;

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<CuotaDTO> findAll() throws Exception {
        return repositorioCuota.findAllConDetalle().stream().map(mapeador::aDTO).toList();
    }

    /** Devuelve la cuota CON sus pagos vigentes (pantalla de detalle). */
    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public CuotaDTO findById(long id) throws Exception {
        return mapeador.aDTOConPagos(buscar(id));
    }

    /** Cuotas de una familia (pantalla de detalle de familia). */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<CuotaDTO> findByGrupoFamiliar(long grupoFamiliarId) throws Exception {
        return repositorioCuota.findByGrupoFamiliarConDetalle(grupoFamiliarId).stream()
                .map(mapeador::aDTO)
                .toList();
    }

    /** Emite UNA cuota a UNA familia. */
    @Override
    public CuotaDTO save(CuotaDTO dto) throws Exception {
        GrupoFamiliar grupo = repositorioGrupo.findById(dto.getGrupoFamiliarId())
                .filter(GrupoFamiliar::isActivo)
                .orElseThrow(() -> new ExcepcionNegocio("La familia seleccionada no existe o fue dada de baja."));

        LocalDate periodo = dto.getPeriodo().atDay(1);
        if (repositorioCuota.existsByGrupoFamiliarIdAndPeriodo(grupo.getId(), periodo)) {
            throw new ExcepcionNegocio("La familia " + grupo.getNombre() + " ya tiene una cuota para "
                    + MapeadorCuota.textoPeriodo(periodo) + " (puede estar anulada).");
        }
        validarVencimiento(periodo, dto.getFechaVencimiento());

        Cuota cuota = mapeador.aEntidad(dto);
        cuota.setGrupoFamiliar(grupo);
        return mapeador.aDTO(repositorioCuota.save(cuota));
    }

    /**
     * Emisión MASIVA: crea la cuota del período a TODAS las familias vigentes que aún no la tengan.
     *
     * @return cantidad de cuotas creadas (las familias que ya tenían la cuota se omiten).
     */
    public int generarParaTodas(GeneracionCuotasDTO dto) throws Exception {
        LocalDate periodo = dto.getPeriodo().atDay(1);
        validarVencimiento(periodo, dto.getFechaVencimiento());

        int creadas = 0;
        for (GrupoFamiliar grupo : repositorioGrupo.findByActivoTrueOrderByNombreAsc()) {
            if (repositorioCuota.existsByGrupoFamiliarIdAndPeriodo(grupo.getId(), periodo)) {
                continue;
            }
            Cuota cuota = new Cuota();
            cuota.setGrupoFamiliar(grupo);
            cuota.setPeriodo(periodo);
            cuota.setImporte(dto.getImporte());
            cuota.setFechaVencimiento(dto.getFechaVencimiento());
            cuota.setEstado(EstadoCuota.PENDIENTE);
            repositorioCuota.save(cuota);
            creadas++;
        }
        return creadas;
    }

    /** Modifica importe y/o vencimiento (el período y la familia no se cambian). */
    @Override
    public CuotaDTO update(long id, CuotaDTO dto) throws Exception {
        Cuota cuota = buscar(id);
        if (cuota.getEstado() == EstadoCuota.ANULADA) {
            throw new ExcepcionNegocio("No se puede modificar una cuota anulada.");
        }
        if (dto.getImporte().compareTo(cuota.getMontoPagado()) < 0) {
            throw new ExcepcionNegocio("El importe no puede ser menor a lo ya pagado ($" + cuota.getMontoPagado() + ").");
        }
        validarVencimiento(cuota.getPeriodo(), dto.getFechaVencimiento());

        mapeador.actualizarEntidad(cuota, dto);
        cuota.actualizarEstado(); // p. ej. si se bajó el importe y ahora lo pagado alcanza
        return mapeador.aDTO(repositorioCuota.save(cuota));
    }

    /** ANULA la cuota (regla 5). */
    @Override
    public void delete(long id) throws Exception {
        Cuota cuota = buscar(id);
        if (cuota.getEstado() == EstadoCuota.ANULADA) {
            throw new ExcepcionNegocio("La cuota ya está anulada.");
        }
        if (cuota.getMontoPagado().signum() > 0) {
            throw new ExcepcionNegocio("La cuota tiene pagos registrados. Anule primero los pagos para poder anularla.");
        }
        cuota.setEstado(EstadoCuota.ANULADA);
        repositorioCuota.save(cuota);
    }

    private Cuota buscar(long id) throws ExcepcionNegocio {
        return repositorioCuota.findByIdConDetalle(id)
                .orElseThrow(() -> new ExcepcionNegocio("La cuota solicitada no existe."));
    }

    /** Regla 2: el vencimiento no puede ser anterior al inicio del período. */
    private void validarVencimiento(LocalDate periodo, LocalDate vencimiento) throws ExcepcionNegocio {
        if (vencimiento.isBefore(periodo)) {
            throw new ExcepcionNegocio("La fecha de vencimiento no puede ser anterior al inicio del período ("
                    + MapeadorCuota.textoPeriodo(periodo) + ").");
        }
    }
}
