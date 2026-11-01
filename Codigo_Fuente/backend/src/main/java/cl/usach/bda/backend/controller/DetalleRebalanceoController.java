package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.entities.DetalleRebalanceoEntity;
import cl.usach.bda.backend.service.DetalleRebalanceoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalles-rebalanceo")
public class DetalleRebalanceoController {

    private final DetalleRebalanceoService service;

    public DetalleRebalanceoController(DetalleRebalanceoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DetalleRebalanceoEntity>> listarDetallesRebalanceo() {
        return ResponseEntity.ok(service.listarDetallesRebalanceo());
    }

    @GetMapping("/orden/{idOrden}")
    public ResponseEntity<List<DetalleRebalanceoEntity>> listarPorOrden(
            @PathVariable("idOrden") Long idOrden) {
        return ResponseEntity.ok(service.listarPorOrden(idOrden));
    }

    @GetMapping("/orden/{idOrden}/vehiculo/{idVehiculo}")
    public ResponseEntity<DetalleRebalanceoEntity> obtenerPorId(
            @PathVariable("idOrden") Long idOrden,
            @PathVariable("idVehiculo") Long idVehiculo) {
        return ResponseEntity.ok(service.obtenerPorId(idOrden, idVehiculo));
    }

    @PostMapping
    public ResponseEntity<DetalleRebalanceoEntity> crear(@RequestBody DetalleRebalanceoEntity detalleRebalanceo) {
        DetalleRebalanceoEntity creado = service.guardarDetalleRebalanceo(detalleRebalanceo);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @DeleteMapping("/orden/{idOrden}/vehiculo/{idVehiculo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable("idOrden") Long idOrden,
            @PathVariable("idVehiculo") Long idVehiculo) {
        service.eliminarDetalleRebalanceo(idOrden, idVehiculo);
        return ResponseEntity.noContent().build();
    }
}
