package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.DisponibilidadEstacionDTO;
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

    // Guardar una nueva estación.
    @Transactional
    public EstacionEntity guardarEstacion(EstacionEntity estacion) {
        return estacionRepository.save(estacion);
    }

    // Listar todas las estaciones.
    public List<EstacionEntity> listarEstaciones() {
        return estacionRepository.findAll();
    }

    // Obtener una estación por su ID.
    public EstacionEntity obtenerEstacionPorId(Long idEstacion) {
        return estacionRepository.findById(idEstacion)
                .orElseThrow(() -> new NoSuchElementException("Estación con ID " + idEstacion + " no encontrada."));
    }

    // Actualizar una estación existente.
    @Transactional
    public EstacionEntity actualizarEstacion(Long idEstacion, EstacionEntity estacion){
        boolean actualizado = estacionRepository.update(idEstacion, estacion);
        if(!actualizado){
            throw new NoSuchElementException("Estación con ID " + idEstacion + " no encontrada para actualizar.");
        }
        return obtenerEstacionPorId(idEstacion);
    }

    // Eliminar una estación por su ID.
    @Transactional
    public void eliminarEstacion(Long idEstacion) {
        boolean eliminado = estacionRepository.deleteById(idEstacion);
        if(!eliminado){
            throw new NoSuchElementException("Estación con ID " + idEstacion + " no encontrada para eliminar.");
        }
    }

    // Obtener la disponibilidad de todas las estaciones.
    public List<DisponibilidadEstacionDTO> obtenerDisponibilidad() {
        return estacionRepository.obtenerDisponibilidad();
    }
}