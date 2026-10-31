package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.SesionDTO;
import cl.usach.bda.backend.repository.UsuarioRepository;
import cl.usach.bda.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    public record ResultadoLogin(String token, SesionDTO sesion) {
    }

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Devuelve vacío tanto si el email no existe, como si el usuario está inactivo o la
    // contraseña es incorrecta: el cliente no debe poder distinguir cuál fue el caso.
    public Optional<ResultadoLogin> autenticar(String email, String password) {
        return usuarioRepository.buscarPorEmail(email)
                .filter(usuario -> Boolean.TRUE.equals(usuario.usuarioActivo()))
                .filter(usuario -> passwordEncoder.matches(password, usuario.passwordHash()))
                .map(usuario -> new ResultadoLogin(
                        jwtService.generarToken(usuario.idUsuario(), usuario.rol()),
                        new SesionDTO(usuario.idUsuario(), usuario.nombreUsuario(),
                                usuario.email(), usuario.rol())
                ));
    }
}
