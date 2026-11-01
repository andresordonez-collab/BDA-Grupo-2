package cl.usach.bda.backend.service;

import cl.usach.bda.backend.entities.DetalleRebalanceoEntity;
import cl.usach.bda.backend.repository.DetalleRebalanceoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DetalleRebalanceoService {

    private final DetalleRebalanceoRepository repository;

    public DetalleRebalanceoService(DetalleRebalanceoRepository repository) {
        this.repository = repository;
    }

    public List<DetalleRebalanceoEntity> listarDetallesRebalanceo() {
        return repository.findAll();
    }

    public List<DetalleRebalanceoEntity> listarPorOrden(Long idOrden) {
        return repository.findByOrdenId(idOrden);
    }

    public DetalleRebalanceoEntity obtenerPorId(Long idOrden, Long idVehiculo) {
        return repository.findById(idOrden, idVehiculo)
                .orElseThrow(() -> noEncontrado(idOrden, idVehiculo));
    }

    @Transactional
    public DetalleRebalanceoEntity guardarDetalleRebalanceo(DetalleRebalanceoEntity detalleRebalanceo) {
        return repository.save(detalleRebalanceo);
    }

    @Transactional
    public void eliminarDetalleRebalanceo(Long idOrden, Long idVehiculo) {
        if (!repository.deleteById(idOrden, idVehiculo)) {
            throw noEncontrado(idOrden, idVehiculo);
        }
    }

    private NoSuchElementException noEncontrado(Long idOrden, Long idVehiculo) {
        return new NoSuchElementException("Detalle de la orden " + idOrden
                + " y vehículo " + idVehiculo + " no encontrado.");
    }
}
