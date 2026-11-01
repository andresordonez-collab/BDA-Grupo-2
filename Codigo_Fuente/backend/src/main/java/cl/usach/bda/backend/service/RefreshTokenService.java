package cl.usach.bda.backend.service;

import cl.usach.bda.backend.entities.RefreshTokenEntity;
import cl.usach.bda.backend.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RefreshTokenEntity guardarRefreshToken(RefreshTokenEntity refreshToken) {
        return repository.save(refreshToken);
    }

    public List<RefreshTokenEntity> listarRefreshTokens() {
        return repository.findAll();
    }

    public RefreshTokenEntity obtenerRefreshTokenPorId(Long idRefreshToken) {
        return repository.findById(idRefreshToken)
                .orElseThrow(() -> new NoSuchElementException("Refresh token con ID " + idRefreshToken + " no encontrado."));
    }

    @Transactional
    public RefreshTokenEntity actualizarRefreshToken(Long idRefreshToken, RefreshTokenEntity refreshToken) {
        boolean actualizado = repository.update(idRefreshToken, refreshToken);
        if (!actualizado) {
            throw new NoSuchElementException("Refresh token con ID " + idRefreshToken + " no encontrado para actualizar.");
        }
        return obtenerRefreshTokenPorId(idRefreshToken);
    }

    @Transactional
    public void eliminarRefreshToken(Long idRefreshToken) {
        boolean eliminado = repository.deleteById(idRefreshToken);
        if (!eliminado) {
            throw new NoSuchElementException("Refresh token con ID " + idRefreshToken + " no encontrado para eliminar.");
        }
    }
}
