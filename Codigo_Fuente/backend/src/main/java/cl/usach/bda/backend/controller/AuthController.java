package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.dto.LoginRequestDTO;
import cl.usach.bda.backend.dto.SesionDTO;
import cl.usach.bda.backend.security.JwtService;
import cl.usach.bda.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String COOKIE_TOKEN = "access_token";

    private final AuthService authService;
    private final JwtService jwtService;
    private final boolean cookieSecure;

    public AuthController(AuthService authService,
                          JwtService jwtService,
                          @Value("${app.cookie.secure}") boolean cookieSecure) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/login")
    public ResponseEntity<SesionDTO> login(@Valid @RequestBody LoginRequestDTO solicitud) {
        return authService.autenticar(solicitud.email(), solicitud.password())
                .map(resultado -> ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, crearCookie(resultado.token()).toString())
                        .body(resultado.sesion()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private ResponseCookie crearCookie(String token) {
        return ResponseCookie.from(COOKIE_TOKEN, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(jwtService.getExpiracionMs()))
                .build();
    }
}
