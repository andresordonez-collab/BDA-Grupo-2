package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.entities.RefreshTokenEntity;
import cl.usach.bda.backend.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refresh-tokens")
public class RefreshTokenController {

    private final RefreshTokenService service;

    public RefreshTokenController(RefreshTokenService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RefreshTokenEntity>> listarRefreshTokens() {
        return ResponseEntity.ok(service.listarRefreshTokens());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefreshTokenEntity> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerRefreshTokenPorId(id));
    }

    @PostMapping
    public ResponseEntity<RefreshTokenEntity> crear(@RequestBody RefreshTokenEntity refreshToken) {
        RefreshTokenEntity creado = service.guardarRefreshToken(refreshToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RefreshTokenEntity> actualizar(
            @PathVariable("id") Long id,
            @RequestBody RefreshTokenEntity refreshToken) {
        return ResponseEntity.ok(service.actualizarRefreshToken(id, refreshToken));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminarRefreshToken(id);
        return ResponseEntity.noContent().build();
    }
}
