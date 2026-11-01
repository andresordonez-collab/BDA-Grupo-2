


package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.dto.CambioEstadoVehiculoDTO;
import cl.usach.bda.backend.entities.VehiculoEntity;
import cl.usach.bda.backend.service.VehiculoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public ResponseEntity<List<VehiculoEntity>> listarVehiculos() {
        return ResponseEntity.ok(vehiculoService.listarVehiculos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoEntity> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(vehiculoService.obtenerVehiculoPorId(id));
    }

    @PostMapping
    public ResponseEntity<VehiculoEntity> crear(@RequestBody VehiculoEntity vehiculo) {
        VehiculoEntity creado = vehiculoService.guardarVehiculo(vehiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculoEntity> actualizar(
            @PathVariable("id") Long id,
            @RequestBody VehiculoEntity vehiculo,
            @RequestParam(value = "idOperador", required = false) Long idOperador,
            @RequestParam(value = "motivo", required = false) String motivo) {
        return ResponseEntity.ok(vehiculoService.actualizarVehiculo(id, vehiculo, idOperador, motivo));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<VehiculoEntity> cambiarEstado(
            @PathVariable("id") Long id,
            @RequestBody CambioEstadoVehiculoDTO solicitud) {

        return ResponseEntity.ok(
                vehiculoService.cambiarEstado(
                        id,
                        solicitud.estadoVehiculo(),
                        solicitud.motivo()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        vehiculoService.eliminarVehiculo(id);
        return ResponseEntity.noContent().build();
    }
}
