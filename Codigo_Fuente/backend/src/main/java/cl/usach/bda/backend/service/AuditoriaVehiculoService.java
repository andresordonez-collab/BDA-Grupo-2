package cl.usach.bda.backend.service;

import cl.usach.bda.backend.entities.AuditoriaVehiculoEntity;
import cl.usach.bda.backend.repository.AuditoriaVehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AuditoriaVehiculoService {

    private final AuditoriaVehiculoRepository repository;

    public AuditoriaVehiculoService(AuditoriaVehiculoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AuditoriaVehiculoEntity guardarAuditoriaVehiculo(AuditoriaVehiculoEntity auditoriaVehiculo) {
        return repository.save(auditoriaVehiculo);
    }

    public List<AuditoriaVehiculoEntity> listarAuditoriasVehiculos() {
        return repository.findAll();
    }

    public AuditoriaVehiculoEntity obtenerAuditoriaVehiculoPorId(Long idAuditoria) {
        return repository.findById(idAuditoria)
                .orElseThrow(() -> new NoSuchElementException("Auditoría de vehículo con ID " + idAuditoria + " no encontrada."));
    }

    @Transactional
    public AuditoriaVehiculoEntity actualizarAuditoriaVehiculo(Long idAuditoria, AuditoriaVehiculoEntity auditoriaVehiculo) {
        boolean actualizado = repository.update(idAuditoria, auditoriaVehiculo);
        if (!actualizado) {
            throw new NoSuchElementException("Auditoría de vehículo con ID " + idAuditoria + " no encontrada para actualizar.");
        }
        return obtenerAuditoriaVehiculoPorId(idAuditoria);
    }

    @Transactional
    public void eliminarAuditoriaVehiculo(Long idAuditoria) {
        boolean eliminado = repository.deleteById(idAuditoria);
        if (!eliminado) {
            throw new NoSuchElementException("Auditoría de vehículo con ID " + idAuditoria + " no encontrada para eliminar.");
        }
    }
}
