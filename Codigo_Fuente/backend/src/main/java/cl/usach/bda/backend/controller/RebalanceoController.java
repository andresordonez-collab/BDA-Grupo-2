package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.dto.RebalanceoRequestDTO;
import cl.usach.bda.backend.service.RebalanceoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rebalanceos")
public class RebalanceoController {
    private final RebalanceoService service;

    public RebalanceoController(RebalanceoService service) {
        this.service = service;
    }

    // Tarea 4: Rebalancea la flota; el operador sale del JWT.
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rebalancear(@Valid @RequestBody RebalanceoRequestDTO solicitud,
                            @AuthenticationPrincipal Long idOperador) {
        service.rebalancear(solicitud, idOperador);
    }
}
