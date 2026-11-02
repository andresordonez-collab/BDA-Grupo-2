package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.dto.DisponibilidadEstacionDTO;
import cl.usach.bda.backend.dto.EstacionDetalleDTO;
import cl.usach.bda.backend.dto.VehiculoDisponibleDTO;
import cl.usach.bda.backend.entities.EstacionEntity;
import cl.usach.bda.backend.service.EstacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estaciones")
public class EstacionController {

    private final EstacionService estacionService;

    public EstacionController(EstacionService estacionService) {
        this.estacionService = estacionService;
    }

    // Tarea 9: Estaciones con vehículos disponibles, con cantidad por tipo y batería promedio.
    @GetMapping("/disponibilidad")
    public List<DisponibilidadEstacionDTO> obtenerDisponibilidad() {
        return estacionService.obtenerDisponibilidad();
    }

    @GetMapping
    public ResponseEntity<List<EstacionDetalleDTO>> listarEstaciones() {
        return ResponseEntity.ok(estacionService.listarEstacionesConDetalle());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstacionEntity> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(estacionService.obtenerEstacionPorId(id));
    }

    @GetMapping("/{id}/vehiculos-disponibles")
    public ResponseEntity<List<VehiculoDisponibleDTO>> listarVehiculosDisponibles(@PathVariable("id") Long id) {
        return ResponseEntity.ok(estacionService.listarVehiculosDisponibles(id));
    }    

    @PostMapping
    public ResponseEntity<EstacionEntity> crear(@RequestBody EstacionEntity estacion) {
        EstacionEntity creada = estacionService.guardarEstacion(estacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstacionEntity> actualizar(
            @PathVariable("id") Long id,
            @RequestBody EstacionEntity estacion) {
        return ResponseEntity.ok(estacionService.actualizarEstacion(id, estacion));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        estacionService.eliminarEstacion(id);
        return ResponseEntity.noContent().build();
    }
}