package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.dto.RebalanceoRequestDTO;
import cl.usach.bda.backend.service.RebalanceoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rebalanceos")
public class RebalanceoController {
    private final RebalanceoService service;

    public RebalanceoController(RebalanceoService service) {
        this.service = service;
    }

    // TODO JWT: vincular/verificar idOperador con el principal y autorizar el rol.
    // El identificador recibido es un dato del cliente, no una identidad autenticada.
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rebalancear(@Valid @RequestBody RebalanceoRequestDTO solicitud) {
        service.rebalancear(solicitud);
    }
}
