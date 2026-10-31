package cl.usach.bda.backend.service;

import cl.usach.bda.backend.entities.TipoVehiculoEntity;
import cl.usach.bda.backend.repository.TipoVehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TipoVehiculoService {

    private final TipoVehiculoRepository tipoVehiculoRepository;

    public TipoVehiculoService(TipoVehiculoRepository tipoVehiculoRepository) {
        this.tipoVehiculoRepository = tipoVehiculoRepository;
    }

    @Transactional
    public TipoVehiculoEntity guardarTipoVehiculo(TipoVehiculoEntity tipoVehiculo) {
        return tipoVehiculoRepository.save(tipoVehiculo);
    }

    public List<TipoVehiculoEntity> listarTiposVehiculos() {
        return tipoVehiculoRepository.findAll();
    }

    public TipoVehiculoEntity obtenerTipoVehiculoPorId(Long idTipoVehiculo) {
        return tipoVehiculoRepository.findById(idTipoVehiculo)
                .orElseThrow(() -> new NoSuchElementException("Tipo de vehículo con ID " + idTipoVehiculo + " no encontrado."));
    }

    @Transactional
    public TipoVehiculoEntity actualizarTipoVehiculo(Long idTipoVehiculo, TipoVehiculoEntity tipoVehiculo) {
        boolean actualizado = tipoVehiculoRepository.update(idTipoVehiculo, tipoVehiculo);
        if (!actualizado) {
            throw new NoSuchElementException("Tipo de vehículo con ID " + idTipoVehiculo + " no encontrado para actualizar.");
        }
        return obtenerTipoVehiculoPorId(idTipoVehiculo);
    }

    @Transactional
    public void eliminarTipoVehiculo(Long idTipoVehiculo) {
        boolean eliminado = tipoVehiculoRepository.deleteById(idTipoVehiculo);
        if (!eliminado) {
            throw new NoSuchElementException("Tipo de vehículo con ID " + idTipoVehiculo + " no encontrado para eliminar.");
        }
    }
}
