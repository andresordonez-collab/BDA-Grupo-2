package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.entities.TipoVehiculoEntity;
import cl.usach.bda.backend.service.TipoVehiculoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-vehiculos")
public class TipoVehiculoController {

    private final TipoVehiculoService tipoVehiculoService;

    public TipoVehiculoController(TipoVehiculoService tipoVehiculoService) {
        this.tipoVehiculoService = tipoVehiculoService;
    }

    @GetMapping
    public ResponseEntity<List<TipoVehiculoEntity>> listarTiposVehiculos() {
        return ResponseEntity.ok(tipoVehiculoService.listarTiposVehiculos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoVehiculoEntity> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(tipoVehiculoService.obtenerTipoVehiculoPorId(id));
    }

    @PostMapping
    public ResponseEntity<TipoVehiculoEntity> crear(@RequestBody TipoVehiculoEntity tipoVehiculo) {
        TipoVehiculoEntity creado = tipoVehiculoService.guardarTipoVehiculo(tipoVehiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoVehiculoEntity> actualizar(
            @PathVariable("id") Long id,
            @RequestBody TipoVehiculoEntity tipoVehiculo) {
        return ResponseEntity.ok(tipoVehiculoService.actualizarTipoVehiculo(id, tipoVehiculo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        tipoVehiculoService.eliminarTipoVehiculo(id);
        return ResponseEntity.noContent().build();
    }
}
