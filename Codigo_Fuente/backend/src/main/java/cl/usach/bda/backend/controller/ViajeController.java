package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.dto.FinalizarViajeRequestDTO;
import cl.usach.bda.backend.dto.IniciarViajeRequestDTO;
import cl.usach.bda.backend.dto.IniciarViajeResponseDTO;
import cl.usach.bda.backend.dto.ResumenMensualDTO;
import cl.usach.bda.backend.dto.ViajeHistorialDTO;
import cl.usach.bda.backend.entities.ViajeEntity;
import cl.usach.bda.backend.service.ViajeService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/viajes")
public class ViajeController {
    private final ViajeService service;

    public ViajeController(ViajeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ViajeEntity>> listarViajes() {
        return ResponseEntity.ok(service.listarViajes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViajeEntity> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerViajePorId(id));
    }

    @PostMapping("/administracion")
    public ResponseEntity<ViajeEntity> crear(
            @RequestBody ViajeEntity viaje) {

        ViajeEntity creado = service.guardarViaje(viaje);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ViajeEntity> actualizar(
            @PathVariable("id") Long id,
            @RequestBody ViajeEntity viaje) {
        return ResponseEntity.ok(service.actualizarViaje(id, viaje));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminarViaje(id);
        return ResponseEntity.noContent().build();
    }

        @PostMapping("/{idViaje}/finalizar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finalizar(@PathVariable("idViaje") Long idViaje,
                          @Valid @RequestBody FinalizarViajeRequestDTO solicitud,
                          @AuthenticationPrincipal Long idUsuario) {
        service.finalizar(idViaje, solicitud.idEstacionDestino(), idUsuario);
    }

    // El usuario sale del JWT: cada usuario solo puede pedir su propio resumen.
    @GetMapping("/resumen-mensual")
    public ResumenMensualDTO resumenMensual(@RequestParam(name = "anio", required = false) Integer anio,
                                            @RequestParam(name = "mes", required = false) Integer mes,
                                            @AuthenticationPrincipal Long idUsuario) {
        return service.resumenMensual(idUsuario, anio, mes);
    }

    // El usuario sale del JWT y la estación origen, del propio vehículo.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IniciarViajeResponseDTO iniciar(@Valid @RequestBody IniciarViajeRequestDTO solicitud,
                                           @AuthenticationPrincipal Long idUsuario) {
        return new IniciarViajeResponseDTO(service.iniciar(solicitud.idVehiculo(), idUsuario));
    }

    // El usuario sale del JWT: solo puede ver su propio historial.
    @GetMapping("/mis-viajes")
    public List<ViajeHistorialDTO> misViajes(@RequestParam(name = "limite", required = false) Integer limite,
                                             @AuthenticationPrincipal Long idUsuario) {
        return service.historial(idUsuario, limite);
    }
}
