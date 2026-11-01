


package cl.usach.bda.backend.service;

import cl.usach.bda.backend.entities.VehiculoEntity;
import cl.usach.bda.backend.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

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
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Long)) {
            throw new AccessDeniedException(
                    "Se requiere una sesión válida para modificar vehículos."
            );
        }

        Long idAutenticado = (Long) authentication.getPrincipal();

        boolean esOperador = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_Operador".equals(authority.getAuthority())
                );

        if (!esOperador) {
            throw new AccessDeniedException(
                    "Solo un operador puede modificar vehículos."
            );
        }

        if (idOperador != null && !idOperador.equals(idAutenticado)) {
            throw new AccessDeniedException(
                    "No puede atribuir el cambio a otro usuario."
            );
        }

    // El ID utilizado por el repositorio siempre procede de la sesión.
        idOperador = idAutenticado;

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

