package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.entities.RolEntity;
import cl.usach.bda.backend.service.RolService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public ResponseEntity<List<RolEntity>> listarRoles() {
        return ResponseEntity.ok(rolService.listarRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolEntity> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(rolService.obtenerRolPorId(id));
    }

    @PostMapping
    public ResponseEntity<RolEntity> crear(@RequestBody RolEntity rol) {
        RolEntity creado = rolService.guardarRol(rol);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolEntity> actualizar(
            @PathVariable("id") Long id,
            @RequestBody RolEntity rol) {
        return ResponseEntity.ok(rolService.actualizarRol(id, rol));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        rolService.eliminarRol(id);
        return ResponseEntity.noContent().build();
    }
}
