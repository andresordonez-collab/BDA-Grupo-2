package cl.usach.bda.backend.security;

import cl.usach.bda.backend.controller.AuthController;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import cl.usach.bda.backend.repository.UsuarioRepository;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    // Tarea 2: Autentica cada solicitud con el JWT de la cookie y comprueba en la BD que el usuario siga activo.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = obtenerToken(request);
            if (token != null) {
                try {
                    Claims claims = jwtService.validarToken(token);
                    Long idUsuario = Long.valueOf(claims.getSubject());

                    if (idUsuario <= 0) {
                        throw new IllegalArgumentException("Identidad JWT inválida.");
                    }

                    var usuario = usuarioRepository.buscarPorId(idUsuario)
                            .filter(u -> Boolean.TRUE.equals(u.usuarioActivo()))
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Usuario inexistente o inactivo."
                            ));

                    String rol = usuario.rol();

                    if (rol == null || rol.isBlank()) {
                        throw new IllegalArgumentException("Usuario sin rol válido.");
                    }

                    var authentication = new UsernamePasswordAuthenticationToken(
                            usuario.idUsuario(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                    );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    var context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                } catch (JwtException | IllegalArgumentException ex) {
                    SecurityContextHolder.clearContext();
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String obtenerToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AuthController.COOKIE_TOKEN.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
