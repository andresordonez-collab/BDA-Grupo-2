package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.dto.ResumenMensualDTO;
import cl.usach.bda.backend.dto.UsuarioResponseDTO;
import cl.usach.bda.backend.entities.UsuarioEntity;
import cl.usach.bda.backend.service.UsuarioService;
import cl.usach.bda.backend.service.ViajeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final ViajeService viajeService;

    public UsuarioController(UsuarioService service, ViajeService viajeService) {
        this.service = service;
        this.viajeService = viajeService;
    }

    // Endpoint para crear un nuevo usuario.
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> guardarUsuario(@RequestBody UsuarioEntity usuario) {
        UsuarioResponseDTO creado = service.guardarUsuario(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // Endpoint para listar todos los usuarios.
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(service.listarUsuarios());
    }

    // Endpoint para obtener un usuario por su ID.
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerUsuarioPorId(id));
    }

    // Endpoint para actualizar un usuario existente.
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(
            @PathVariable("id") Long id,
            @RequestBody UsuarioEntity usuario) {
        return ResponseEntity.ok(service.actualizarUsuario(id, usuario));
    }

    // Endpoint para eliminar un usuario por su ID.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable("id") Long id) {
        service.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    // Resumen mensual de cualquier usuario (ruta solo para Operador).
    @GetMapping("/{id}/resumen-mensual")
    public ResponseEntity<ResumenMensualDTO> resumenMensual(
            @PathVariable("id") Long id,
            @RequestParam(name = "anio", required = false) Integer anio,
            @RequestParam(name = "mes", required = false) Integer mes) {
        service.obtenerUsuarioPorId(id); // 404 si el usuario no existe
        return ResponseEntity.ok(viajeService.resumenMensual(id, anio, mes));
    }
}
