package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.entities.MovimientoBilleteraEntity;
import cl.usach.bda.backend.service.MovimientoBilleteraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos-billeteras")
public class MovimientoBilleteraController {

    private final MovimientoBilleteraService movimientoBilleteraService;

    public MovimientoBilleteraController(MovimientoBilleteraService movimientoBilleteraService) {
        this.movimientoBilleteraService = movimientoBilleteraService;
    }

    @GetMapping
    public ResponseEntity<List<MovimientoBilleteraEntity>> listarMovimientosBilleteras() {
        return ResponseEntity.ok(movimientoBilleteraService.listarMovimientosBilleteras());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoBilleteraEntity> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(movimientoBilleteraService.obtenerMovimientoBilleteraPorId(id));
    }

    @PostMapping
    public ResponseEntity<MovimientoBilleteraEntity> crear(@RequestBody MovimientoBilleteraEntity movimientoBilletera) {
        MovimientoBilleteraEntity creado = movimientoBilleteraService.guardarMovimientoBilletera(movimientoBilletera);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovimientoBilleteraEntity> actualizar(
            @PathVariable("id") Long id,
            @RequestBody MovimientoBilleteraEntity movimientoBilletera) {
        return ResponseEntity.ok(movimientoBilleteraService.actualizarMovimientoBilletera(id, movimientoBilletera));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        movimientoBilleteraService.eliminarMovimientoBilletera(id);
        return ResponseEntity.noContent().build();
    }
}
