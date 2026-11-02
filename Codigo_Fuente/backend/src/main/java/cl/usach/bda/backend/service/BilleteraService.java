package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.MiBilleteraDTO;
import cl.usach.bda.backend.entities.BilleteraEntity;
import cl.usach.bda.backend.entities.UsuarioEntity;
import cl.usach.bda.backend.repository.BilleteraRepository;
import cl.usach.bda.backend.repository.RolRepository;
import cl.usach.bda.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BilleteraService {

    private final BilleteraRepository billeteraRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public BilleteraService(BilleteraRepository billeteraRepository,
                            UsuarioRepository usuarioRepository,
                            RolRepository rolRepository) {
        this.billeteraRepository = billeteraRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Transactional
    public BilleteraEntity guardarBilletera(BilleteraEntity billetera) {
        if (billetera.getIdUsuario() == null) {
            throw new IllegalArgumentException("Debe indicar el usuario de la billetera.");
        }

        UsuarioEntity usuario = usuarioRepository.findById(billetera.getIdUsuario())
                .orElseThrow(() -> new NoSuchElementException(
                        "Usuario con ID " + billetera.getIdUsuario() + " no encontrado."));
        boolean esUsuario = rolRepository.findById(usuario.getIdRol())
                .map(rol -> "Usuario".equals(rol.getNombreRol()))
                .orElse(false);
        if (!esUsuario) {
            throw new IllegalArgumentException("Solo los usuarios con rol Usuario tienen billetera.");
        }
        if (billeteraRepository.findByIdUsuario(billetera.getIdUsuario()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya tiene una billetera.");
        }

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
        BilleteraEntity existente = obtenerBilleteraPorId(idBilletera);

        if (billetera.getIdUsuario() != null
                && !billetera.getIdUsuario().equals(existente.getIdUsuario())) {
            throw new IllegalArgumentException("No se puede cambiar el usuario dueño de una billetera.");
        }

        billetera.setIdUsuario(existente.getIdUsuario());
        boolean actualizado = billeteraRepository.update(idBilletera, billetera);
        if (!actualizado) {
            throw new NoSuchElementException("Billetera con ID " + idBilletera + " no encontrada para actualizar.");
        }
        return obtenerBilleteraPorId(idBilletera);
    }

    @Transactional
    public void eliminarBilletera(Long idBilletera) {
        obtenerBilleteraPorId(idBilletera);
        if (billeteraRepository.duenoTieneViajeActivo(idBilletera)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar la billetera: su dueño tiene un viaje activo y no podría finalizarlo.");
        }
        boolean eliminado = billeteraRepository.deleteById(idBilletera);
        if (!eliminado) {
            throw new NoSuchElementException("Billetera con ID " + idBilletera + " no encontrada para eliminar.");
        }
    }

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
