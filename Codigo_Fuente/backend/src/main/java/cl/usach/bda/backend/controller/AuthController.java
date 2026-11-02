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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

    // Tarea 2: Inicia sesión y entrega el JWT en una cookie HttpOnly.
    @PostMapping("/login")
    public ResponseEntity<SesionDTO> login(@Valid @RequestBody LoginRequestDTO solicitud) {
        return authService.autenticar(solicitud.email(), solicitud.password())
                .map(resultado -> ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE,
                                crearCookie(resultado.token(), Duration.ofMillis(jwtService.getExpiracionMs())).toString())
                        .body(resultado.sesion()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // Tarea 2: Devuelve el usuario de la sesión: el id sale del JWT y los datos, de la BD.
    @GetMapping("/me")
    public ResponseEntity<SesionDTO> me(@AuthenticationPrincipal Long idUsuario) {
        return authService.obtenerSesion(idUsuario)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header(HttpHeaders.SET_COOKIE, crearCookie("", Duration.ZERO).toString())
                        .build());
    }

    // Tarea 2: Cierra la sesión borrando la cookie del JWT.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, crearCookie("", Duration.ZERO).toString())
                .build();
    }

    private ResponseCookie crearCookie(String valor, Duration duracion) {
        return ResponseCookie.from(COOKIE_TOKEN, valor)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(duracion)
                .build();
    }
}
