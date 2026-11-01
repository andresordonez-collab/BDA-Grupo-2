package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.dto.DisponibilidadEstacionDTO;
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

    // Endpoint para obtener la disponibilidad de todas las estaciones.
    @GetMapping("/disponibilidad")
    public List<DisponibilidadEstacionDTO> obtenerDisponibilidad() {
        return estacionService.obtenerDisponibilidad();
    }

    // Endpoint para listar todas las estaciones.
    @GetMapping
    public ResponseEntity<List<EstacionEntity>> listarEstaciones() {
        return ResponseEntity.ok(estacionService.listarEstaciones());
    }

    // Endpoint para obtener una estación por su ID.
    @GetMapping("/{id}")
    public ResponseEntity<EstacionEntity> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(estacionService.obtenerEstacionPorId(id));
    }

    // Vehículos que se pueden tomar en una estación (lectura para cualquier usuario autenticado).
    @GetMapping("/{id}/vehiculos-disponibles")
    public ResponseEntity<List<VehiculoDisponibleDTO>> listarVehiculosDisponibles(@PathVariable("id") Long id) {
        return ResponseEntity.ok(estacionService.listarVehiculosDisponibles(id));
    }    

    // Endpoint para crear una nueva estación.
    @PostMapping
    public ResponseEntity<EstacionEntity> crear(@RequestBody EstacionEntity estacion) {
        EstacionEntity creada = estacionService.guardarEstacion(estacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // Endpoint para actualizar una estación existente.
    @PutMapping("/{id}")
    public ResponseEntity<EstacionEntity> actualizar(
            @PathVariable("id") Long id,
            @RequestBody EstacionEntity estacion) {
        return ResponseEntity.ok(estacionService.actualizarEstacion(id, estacion));
    }
    
    // Endpoint para eliminar una estación por su ID.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        estacionService.eliminarEstacion(id);
        return ResponseEntity.noContent().build();
    }
}