package cl.usach.bda.backend.service;

import cl.usach.bda.backend.entities.MovimientoBilleteraEntity;
import cl.usach.bda.backend.repository.MovimientoBilleteraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MovimientoBilleteraService {

    private final MovimientoBilleteraRepository movimientoBilleteraRepository;

    public MovimientoBilleteraService(MovimientoBilleteraRepository movimientoBilleteraRepository) {
        this.movimientoBilleteraRepository = movimientoBilleteraRepository;
    }

    @Transactional
    public MovimientoBilleteraEntity guardarMovimientoBilletera(MovimientoBilleteraEntity movimientoBilletera) {
        return movimientoBilleteraRepository.save(movimientoBilletera);
    }

    public List<MovimientoBilleteraEntity> listarMovimientosBilleteras() {
        return movimientoBilleteraRepository.findAll();
    }

    public MovimientoBilleteraEntity obtenerMovimientoBilleteraPorId(Long idMovimiento) {
        return movimientoBilleteraRepository.findById(idMovimiento)
                .orElseThrow(() -> new NoSuchElementException("Movimiento de billetera con ID " + idMovimiento + " no encontrado."));
    }

    @Transactional
    public MovimientoBilleteraEntity actualizarMovimientoBilletera(Long idMovimiento, MovimientoBilleteraEntity movimientoBilletera) {
        boolean actualizado = movimientoBilleteraRepository.update(idMovimiento, movimientoBilletera);
        if (!actualizado) {
            throw new NoSuchElementException("Movimiento de billetera con ID " + idMovimiento + " no encontrado para actualizar.");
        }
        return obtenerMovimientoBilleteraPorId(idMovimiento);
    }

    @Transactional
    public void eliminarMovimientoBilletera(Long idMovimiento) {
        boolean eliminado = movimientoBilleteraRepository.deleteById(idMovimiento);
        if (!eliminado) {
            throw new NoSuchElementException("Movimiento de billetera con ID " + idMovimiento + " no encontrado para eliminar.");
        }
    }
}
