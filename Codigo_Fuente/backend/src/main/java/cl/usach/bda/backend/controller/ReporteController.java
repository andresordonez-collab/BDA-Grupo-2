package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.dto.DemandaEstacionHorariaDTO;
import cl.usach.bda.backend.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/demanda-estacion-horaria")
    public ResponseEntity<List<DemandaEstacionHorariaDTO>> obtenerDemandaEstacionHoraria() {
        return ResponseEntity.ok(reporteService.obtenerDemandaEstacionHoraria());
    }
}
