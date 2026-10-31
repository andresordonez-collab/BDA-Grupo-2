package cl.usach.bda.backend.service;

import cl.usach.bda.backend.entities.TarifaEntity;
import cl.usach.bda.backend.repository.TarifaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TarifaService {

    private final TarifaRepository tarifaRepository;

    public TarifaService(TarifaRepository tarifaRepository) {
        this.tarifaRepository = tarifaRepository;
    }

    @Transactional
    public TarifaEntity guardarTarifa(TarifaEntity tarifa) {
        return tarifaRepository.save(tarifa);
    }

    public List<TarifaEntity> listarTarifas() {
        return tarifaRepository.findAll();
    }

    public TarifaEntity obtenerTarifaPorId(Long idTarifa) {
        return tarifaRepository.findById(idTarifa)
                .orElseThrow(() -> new NoSuchElementException("Tarifa con ID " + idTarifa + " no encontrada."));
    }

    @Transactional
    public TarifaEntity actualizarTarifa(Long idTarifa, TarifaEntity tarifa) {
        boolean actualizado = tarifaRepository.update(idTarifa, tarifa);
        if (!actualizado) {
            throw new NoSuchElementException("Tarifa con ID " + idTarifa + " no encontrada para actualizar.");
        }
        return obtenerTarifaPorId(idTarifa);
    }

    @Transactional
    public void eliminarTarifa(Long idTarifa) {
        boolean eliminado = tarifaRepository.deleteById(idTarifa);
        if (!eliminado) {
            throw new NoSuchElementException("Tarifa con ID " + idTarifa + " no encontrada para eliminar.");
        }
    }
}
