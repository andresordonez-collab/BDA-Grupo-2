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

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = obtenerToken(request);
            if (token != null) {
                try {
                    Claims claims = jwtService.validarToken(token);
                    Long idUsuario = Long.valueOf(claims.getSubject());
                    String rol = claims.get("rol", String.class);
                    if (idUsuario <= 0 || rol == null || rol.isBlank()) {
                        throw new IllegalArgumentException("Identidad JWT inválida");
                    }
                    var authentication = new UsernamePasswordAuthenticationToken(
                            idUsuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    var context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                } catch (JwtException | IllegalArgumentException ex) {
                    // Sin autenticación, las rutas protegidas responderán 401.
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
