package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.UsuarioResponseDTO;
import cl.usach.bda.backend.entities.UsuarioEntity;
import cl.usach.bda.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UsuarioResponseDTO toResponse(UsuarioEntity usuario) {
        return new UsuarioResponseDTO(
                usuario.getIdUsuario(),
                usuario.getIdRol(),
                usuario.getNombreUsuario(),
                usuario.getEmail(),
                usuario.getUsuarioActivo(),
                usuario.getFechaRegistro()
        );
    }

    private UsuarioEntity buscarUsuarioExistente(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new NoSuchElementException(
                        "Usuario con ID " + idUsuario + " no encontrado."
                ));
    }

    private String codificarPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        return passwordEncoder.encode(password);
    }

    @Transactional
    public UsuarioResponseDTO guardarUsuario(UsuarioEntity usuario) {
        usuario.setPasswordHash(codificarPassword(usuario.getPasswordHash()));

        UsuarioEntity creado = usuarioRepository.save(usuario);

        return toResponse(buscarUsuarioExistente(creado.getIdUsuario()));
    }

    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UsuarioResponseDTO obtenerUsuarioPorId(Long idUsuario) {
        return toResponse(buscarUsuarioExistente(idUsuario));
    }

    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long idUsuario, UsuarioEntity usuario) {

        UsuarioEntity existente = buscarUsuarioExistente(idUsuario);

        if (usuario.getPasswordHash() == null) {
            usuario.setPasswordHash(existente.getPasswordHash());
        } else {
            usuario.setPasswordHash(codificarPassword(usuario.getPasswordHash()));
        }
        usuario.setFechaRegistro(existente.getFechaRegistro());

        boolean actualizado = usuarioRepository.update(idUsuario,usuario);

        if (!actualizado) {
            throw new NoSuchElementException(
                    "Usuario con ID " + idUsuario
                            + " no encontrado para actualizar."
            );
        }

        return toResponse(buscarUsuarioExistente(idUsuario));
    }

    @Transactional
    public void eliminarUsuario(Long idUsuario) {
        boolean eliminado = usuarioRepository.deleteById(idUsuario);

        if (!eliminado) {
            throw new NoSuchElementException(
                    "Usuario con ID " + idUsuario
                            + " no encontrado para eliminar."
            );
        }
    }
}