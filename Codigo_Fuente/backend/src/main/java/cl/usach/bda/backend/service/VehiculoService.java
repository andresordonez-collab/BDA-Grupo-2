


package cl.usach.bda.backend.service;

import cl.usach.bda.backend.entities.VehiculoEntity;
import cl.usach.bda.backend.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    @Transactional
    public VehiculoEntity guardarVehiculo(VehiculoEntity vehiculo) {
        return vehiculoRepository.save(vehiculo);
    }

    public List<VehiculoEntity> listarVehiculos() {
        return vehiculoRepository.findAll();
    }

    public VehiculoEntity obtenerVehiculoPorId(Long idVehiculo) {
        return vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new NoSuchElementException("Vehículo con ID " + idVehiculo + " no encontrado."));
    }

    @Transactional
    public VehiculoEntity actualizarVehiculo(Long idVehiculo, VehiculoEntity vehiculo,
                                             Long idOperador, String motivo) {
        VehiculoEntity existente = obtenerVehiculoPorId(idVehiculo);
        if (idOperador == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Long) {
                idOperador = (Long) authentication.getPrincipal();
            }
        }
        boolean requiereAuditoria = ("En mantención".equals(vehiculo.getEstadoVehiculo())
                || "Dado de baja".equals(vehiculo.getEstadoVehiculo()))
                && !vehiculo.getEstadoVehiculo().equals(existente.getEstadoVehiculo());
        if (requiereAuditoria && idOperador == null) {
            throw new IllegalArgumentException("Debe indicar idOperador para auditar el cambio de estado.");
        }
        boolean actualizado = vehiculoRepository.update(idVehiculo, vehiculo, idOperador, motivo);
        if (!actualizado) {
            throw new NoSuchElementException("Vehículo con ID " + idVehiculo + " no encontrado para actualizar.");
        }
        return obtenerVehiculoPorId(idVehiculo);
    }

    @Transactional
    public void eliminarVehiculo(Long idVehiculo) {
        boolean eliminado = vehiculoRepository.deleteById(idVehiculo);
        if (!eliminado) {
            throw new NoSuchElementException("Vehículo con ID " + idVehiculo + " no encontrado para eliminar.");
        }
    }
}

