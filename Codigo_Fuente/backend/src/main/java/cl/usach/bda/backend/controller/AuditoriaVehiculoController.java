package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.entities.AuditoriaVehiculoEntity;
import cl.usach.bda.backend.service.AuditoriaVehiculoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditorias-vehiculos")
public class AuditoriaVehiculoController {

    private final AuditoriaVehiculoService service;

    public AuditoriaVehiculoController(AuditoriaVehiculoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AuditoriaVehiculoEntity>> listarAuditoriasVehiculos() {
        return ResponseEntity.ok(service.listarAuditoriasVehiculos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditoriaVehiculoEntity> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerAuditoriaVehiculoPorId(id));
    }

    @PostMapping
    public ResponseEntity<AuditoriaVehiculoEntity> crear(@RequestBody AuditoriaVehiculoEntity auditoriaVehiculo) {
        AuditoriaVehiculoEntity creado = service.guardarAuditoriaVehiculo(auditoriaVehiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuditoriaVehiculoEntity> actualizar(
            @PathVariable("id") Long id,
            @RequestBody AuditoriaVehiculoEntity auditoriaVehiculo) {
        return ResponseEntity.ok(service.actualizarAuditoriaVehiculo(id, auditoriaVehiculo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminarAuditoriaVehiculo(id);
        return ResponseEntity.noContent().build();
    }
}
