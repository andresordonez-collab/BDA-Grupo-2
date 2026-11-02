package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.OrdenRebalanceoDetalleDTO;
import cl.usach.bda.backend.entities.OrdenRebalanceoEntity;
import cl.usach.bda.backend.repository.OrdenRebalanceoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrdenRebalanceoService {

    private final OrdenRebalanceoRepository repository;

    public OrdenRebalanceoService(OrdenRebalanceoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrdenRebalanceoEntity guardarOrdenRebalanceo(OrdenRebalanceoEntity ordenRebalanceo) {
        return repository.save(ordenRebalanceo);
    }

    public List<OrdenRebalanceoEntity> listarOrdenesRebalanceo() {
        return repository.findAll();
    }

    public List<OrdenRebalanceoDetalleDTO> listarOrdenesRebalanceoConDetalle() {
        return repository.findAllConDetalle();
    }

    public OrdenRebalanceoEntity obtenerOrdenRebalanceoPorId(Long idOrden) {
        return repository.findById(idOrden)
                .orElseThrow(() -> new NoSuchElementException("Orden de rebalanceo con ID " + idOrden + " no encontrada."));
    }

    @Transactional
    public OrdenRebalanceoEntity actualizarOrdenRebalanceo(Long idOrden, OrdenRebalanceoEntity ordenRebalanceo) {
        boolean actualizado = repository.update(idOrden, ordenRebalanceo);
        if (!actualizado) {
            throw new NoSuchElementException("Orden de rebalanceo con ID " + idOrden + " no encontrada para actualizar.");
        }
        return obtenerOrdenRebalanceoPorId(idOrden);
    }

    @Transactional
    public void eliminarOrdenRebalanceo(Long idOrden) {
        boolean eliminado = repository.deleteById(idOrden);
        if (!eliminado) {
            throw new NoSuchElementException("Orden de rebalanceo con ID " + idOrden + " no encontrada para eliminar.");
        }
    }
}
