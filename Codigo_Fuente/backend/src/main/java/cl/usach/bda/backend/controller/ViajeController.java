package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.dto.FinalizarViajeRequestDTO;
import cl.usach.bda.backend.service.ViajeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/viajes")
public class ViajeController {
    private final ViajeService service;

    public ViajeController(ViajeService service) {
        this.service = service;
    }

    @PostMapping("/{idViaje}/finalizar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finalizar(@PathVariable("idViaje") Long idViaje,
                          @Valid @RequestBody FinalizarViajeRequestDTO solicitud,
                          @AuthenticationPrincipal Long idUsuario) {
        service.finalizar(idViaje, solicitud.idEstacionDestino(), idUsuario);
    }
}
