package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.MiBilleteraDTO;
import cl.usach.bda.backend.entities.BilleteraEntity;
import cl.usach.bda.backend.repository.BilleteraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BilleteraService {

    private final BilleteraRepository billeteraRepository;

    public BilleteraService(BilleteraRepository billeteraRepository) {
        this.billeteraRepository = billeteraRepository;
    }

    @Transactional
    public BilleteraEntity guardarBilletera(BilleteraEntity billetera) {
        return billeteraRepository.save(billetera);
    }

    public List<BilleteraEntity> listarBilleteras() {
        return billeteraRepository.findAll();
    }

    public BilleteraEntity obtenerBilleteraPorId(Long idBilletera) {
        return billeteraRepository.findById(idBilletera)
                .orElseThrow(() -> new NoSuchElementException("Billetera con ID " + idBilletera + " no encontrada."));
    }

    @Transactional
    public BilleteraEntity actualizarBilletera(Long idBilletera, BilleteraEntity billetera) {
        boolean actualizado = billeteraRepository.update(idBilletera, billetera);
        if (!actualizado) {
            throw new NoSuchElementException("Billetera con ID " + idBilletera + " no encontrada para actualizar.");
        }
        return obtenerBilleteraPorId(idBilletera);
    }

    @Transactional
    public void eliminarBilletera(Long idBilletera) {
        boolean eliminado = billeteraRepository.deleteById(idBilletera);
        if (!eliminado) {
            throw new NoSuchElementException("Billetera con ID " + idBilletera + " no encontrada para eliminar.");
        }
    }

    // El usuario sale del JWT: cada usuario solo puede ver su propia billetera.
    public MiBilleteraDTO obtenerMiBilletera(Long idUsuario, Integer limite) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("El identificador de usuario debe ser un entero positivo.");
        }
        int tope = limite != null ? limite : 20;
        if (tope < 1 || tope > 100) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 100.");
        }
        BilleteraEntity billetera = billeteraRepository.findByIdUsuario(idUsuario)
                .orElseThrow(() -> new NoSuchElementException("El usuario no tiene una billetera asignada."));
        return new MiBilleteraDTO(
                billetera.getIdBilletera(),
                billetera.getSaldo(),
                billeteraRepository.listarMovimientosRecientes(billetera.getIdBilletera(), tope));
    }
}
