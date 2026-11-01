package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.DemandaEstacionHorariaDTO;
import cl.usach.bda.backend.repository.ReporteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;

    public ReporteService(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    public List<DemandaEstacionHorariaDTO> obtenerDemandaEstacionHoraria() {
        return reporteRepository.obtenerDemandaEstacionHoraria();
    }
}
