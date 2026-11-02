


package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.VehiculoDetalleDTO;
import cl.usach.bda.backend.entities.TipoVehiculoEntity;
import cl.usach.bda.backend.entities.VehiculoEntity;
import cl.usach.bda.backend.repository.TipoVehiculoRepository;
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
    private final TipoVehiculoRepository tipoVehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository,
                           TipoVehiculoRepository tipoVehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.tipoVehiculoRepository = tipoVehiculoRepository;
    }

    // Una bicicleta no tiene batería; un scooter la necesita (entre 0 y 100) para poder iniciar viajes.
    private void validarBateriaSegunTipo(VehiculoEntity vehiculo) {
        if (vehiculo.getIdTipoVehiculo() == null) {
            throw new IllegalArgumentException("Debe indicar el tipo de vehículo.");
        }

        String tipo = tipoVehiculoRepository.findById(vehiculo.getIdTipoVehiculo())
                .map(TipoVehiculoEntity::getNombreTipoVehiculo)
                .orElseThrow(() -> new IllegalArgumentException("El tipo de vehículo indicado no existe."));
        Integer bateria = vehiculo.getNivelBateria();

        if ("Bicicleta".equals(tipo) && bateria != null) {
            throw new IllegalArgumentException(
                    "Una bicicleta no tiene batería: el nivel de batería debe quedar vacío.");
        }
        if ("Scooter".equals(tipo) && (bateria == null || bateria < 0 || bateria > 100)) {
            throw new IllegalArgumentException(
                    "Un scooter requiere un nivel de batería entre 0 y 100.");
        }
    }

    @Transactional
    public VehiculoEntity guardarVehiculo(VehiculoEntity vehiculo) {
        validarBateriaSegunTipo(vehiculo);
        return vehiculoRepository.save(vehiculo);
    }

    public List<VehiculoEntity> listarVehiculos() {
        return vehiculoRepository.findAll();
    }

    public List<VehiculoDetalleDTO> listarVehiculosConDetalle() {
        return vehiculoRepository.findAllConDetalle();
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

        validarBateriaSegunTipo(vehiculo);

        boolean actualizado = vehiculoRepository.update(idVehiculo, vehiculo, idOperador, motivo);
        if (!actualizado) {
            throw new NoSuchElementException("Vehículo con ID " + idVehiculo + " no encontrado para actualizar.");
        }
        return obtenerVehiculoPorId(idVehiculo);
    }

    @Transactional
    public VehiculoEntity cambiarEstado(Long idVehiculo, String estadoVehiculo,
                                        String motivo) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Long)) {
            throw new AccessDeniedException(
                    "Se requiere una sesión válida para modificar vehículos."
            );
        }

        boolean esOperador = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_Operador".equals(authority.getAuthority())
                );

        if (!esOperador) {
            throw new AccessDeniedException(
                    "Solo un operador puede modificar vehículos."
            );
        }

        if (idVehiculo == null || idVehiculo <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del vehículo debe ser un entero positivo."
            );
        }

        if (estadoVehiculo == null || !List.of(
                "Disponible",
                "En uso",
                "En mantención",
                "Dado de baja"
        ).contains(estadoVehiculo)) {
            throw new IllegalArgumentException(
                    "El estado del vehículo no es válido."
            );
        }

        boolean requiereMotivo = "En mantención".equals(estadoVehiculo)
                || "Dado de baja".equals(estadoVehiculo);

        if (requiereMotivo && (motivo == null || motivo.isBlank())) {
            throw new IllegalArgumentException(
                    "Debe indicar un motivo para mantención o baja."
            );
        }

        Long idOperador = (Long) authentication.getPrincipal();

        boolean actualizado = vehiculoRepository.updateEstado(
                idVehiculo,
                estadoVehiculo,
                idOperador,
                motivo != null ? motivo.trim() : ""
        );

        if (!actualizado) {
            throw new NoSuchElementException(
                    "Vehículo con ID " + idVehiculo
                            + " no encontrado para actualizar."
            );
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

