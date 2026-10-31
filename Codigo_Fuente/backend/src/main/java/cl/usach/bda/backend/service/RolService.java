package cl.usach.bda.backend.service;

import cl.usach.bda.backend.entities.RolEntity;
import cl.usach.bda.backend.repository.RolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Transactional
    public RolEntity guardarRol(RolEntity rol) {
        return rolRepository.save(rol);
    }

    public List<RolEntity> listarRoles() {
        return rolRepository.findAll();
    }

    public RolEntity obtenerRolPorId(Long idRol) {
        return rolRepository.findById(idRol)
                .orElseThrow(() -> new NoSuchElementException("Rol con ID " + idRol + " no encontrado."));
    }

    @Transactional
    public RolEntity actualizarRol(Long idRol, RolEntity rol) {
        boolean actualizado = rolRepository.update(idRol, rol);
        if (!actualizado) {
            throw new NoSuchElementException("Rol con ID " + idRol + " no encontrado para actualizar.");
        }
        return obtenerRolPorId(idRol);
    }

    @Transactional
    public void eliminarRol(Long idRol) {
        boolean eliminado = rolRepository.deleteById(idRol);
        if (!eliminado) {
            throw new NoSuchElementException("Rol con ID " + idRol + " no encontrado para eliminar.");
        }
    }
}
