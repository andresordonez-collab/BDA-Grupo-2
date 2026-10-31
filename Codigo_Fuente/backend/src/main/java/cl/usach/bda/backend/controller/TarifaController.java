package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.entities.TarifaEntity;
import cl.usach.bda.backend.service.TarifaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tarifas")
public class TarifaController {

    private final TarifaService tarifaService;

    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping
    public ResponseEntity<List<TarifaEntity>> listarTarifas() {
        return ResponseEntity.ok(tarifaService.listarTarifas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarifaEntity> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(tarifaService.obtenerTarifaPorId(id));
    }

    @PostMapping
    public ResponseEntity<TarifaEntity> crear(@RequestBody TarifaEntity tarifa) {
        TarifaEntity creado = tarifaService.guardarTarifa(tarifa);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarifaEntity> actualizar(
            @PathVariable("id") Long id,
            @RequestBody TarifaEntity tarifa) {
        return ResponseEntity.ok(tarifaService.actualizarTarifa(id, tarifa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        tarifaService.eliminarTarifa(id);
        return ResponseEntity.noContent().build();
    }
}
