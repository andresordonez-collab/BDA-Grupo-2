package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.DisponibilidadEstacionDTO;
import cl.usach.bda.backend.repository.EstacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstacionService {

    private final EstacionRepository estacionRepository;

    public EstacionService(EstacionRepository estacionRepository) {
        this.estacionRepository = estacionRepository;
    }

    public List<DisponibilidadEstacionDTO> obtenerDisponibilidad() {
        return estacionRepository.obtenerDisponibilidad();
    }
}