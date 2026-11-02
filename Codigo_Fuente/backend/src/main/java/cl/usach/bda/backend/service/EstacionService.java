package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.DisponibilidadEstacionDTO;
import cl.usach.bda.backend.dto.EstacionDetalleDTO;
import cl.usach.bda.backend.dto.VehiculoDisponibleDTO;
import cl.usach.bda.backend.entities.EstacionEntity;
import cl.usach.bda.backend.repository.EstacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EstacionService {

    private final EstacionRepository estacionRepository;

    public EstacionService(EstacionRepository estacionRepository) {
        this.estacionRepository = estacionRepository;
    }

    @Transactional
    public EstacionEntity guardarEstacion(EstacionEntity estacion) {
        return estacionRepository.save(estacion);
    }

    public List<EstacionEntity> listarEstaciones() {
        return estacionRepository.findAll();
    }

    public List<EstacionDetalleDTO> listarEstacionesConDetalle() {
        return estacionRepository.findAllConDetalle();
    }

    public EstacionEntity obtenerEstacionPorId(Long idEstacion) {
        return estacionRepository.findById(idEstacion)
                .orElseThrow(() -> new NoSuchElementException("Estación con ID " + idEstacion + " no encontrada."));
    }

    @Transactional
    public EstacionEntity actualizarEstacion(Long idEstacion, EstacionEntity estacion){
        boolean actualizado = estacionRepository.update(idEstacion, estacion);
        if(!actualizado){
            throw new NoSuchElementException("Estación con ID " + idEstacion + " no encontrada para actualizar.");
        }
        return obtenerEstacionPorId(idEstacion);
    }

    @Transactional
    public void eliminarEstacion(Long idEstacion) {
        boolean eliminado = estacionRepository.deleteById(idEstacion);
        if(!eliminado){
            throw new NoSuchElementException("Estación con ID " + idEstacion + " no encontrada para eliminar.");
        }
    }

    public List<DisponibilidadEstacionDTO> obtenerDisponibilidad() {
        return estacionRepository.obtenerDisponibilidad();
    }

    public List<VehiculoDisponibleDTO> listarVehiculosDisponibles(Long idEstacion) {
        obtenerEstacionPorId(idEstacion);
        return estacionRepository.listarVehiculosDisponibles(idEstacion);
    }    
}