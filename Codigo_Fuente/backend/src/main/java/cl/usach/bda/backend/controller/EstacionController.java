package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.dto.DisponibilidadEstacionDTO;
import cl.usach.bda.backend.service.EstacionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/estaciones")
public class EstacionController {

    private final EstacionService estacionService;

    public EstacionController(EstacionService estacionService) {
        this.estacionService = estacionService;
    }

    @GetMapping("/disponibilidad")
    public List<DisponibilidadEstacionDTO> obtenerDisponibilidad() {
        return estacionService.obtenerDisponibilidad();
    }
}