package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.entities.OrdenRebalanceoEntity;
import cl.usach.bda.backend.service.OrdenRebalanceoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-rebalanceo")
public class OrdenRebalanceoController {

    private final OrdenRebalanceoService service;

    public OrdenRebalanceoController(OrdenRebalanceoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<OrdenRebalanceoEntity>> listarOrdenesRebalanceo() {
        return ResponseEntity.ok(service.listarOrdenesRebalanceo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenRebalanceoEntity> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerOrdenRebalanceoPorId(id));
    }

    @PostMapping
    public ResponseEntity<OrdenRebalanceoEntity> crear(@RequestBody OrdenRebalanceoEntity ordenRebalanceo) {
        OrdenRebalanceoEntity creado = service.guardarOrdenRebalanceo(ordenRebalanceo);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenRebalanceoEntity> actualizar(
            @PathVariable("id") Long id,
            @RequestBody OrdenRebalanceoEntity ordenRebalanceo) {
        return ResponseEntity.ok(service.actualizarOrdenRebalanceo(id, ordenRebalanceo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminarOrdenRebalanceo(id);
        return ResponseEntity.noContent().build();
    }
}
