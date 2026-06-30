package com.vetexpert.sistema_veterinaria.historias.controller.api;

import com.vetexpert.sistema_veterinaria.historias.dto.HistoriaClinicaDTO;
import com.vetexpert.sistema_veterinaria.historias.entity.HistoriaClinica;
import com.vetexpert.sistema_veterinaria.historias.service.HistoriaClinicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para exponer el historial clínico a sistemas externos u otras aplicaciones (APIs).
 * Módulo: historias
 */
@RestController
@RequestMapping("/api/historias")
public class HistoriaClinicaRestController {

    private final HistoriaClinicaService historiaClinicaService;

    public HistoriaClinicaRestController(HistoriaClinicaService historiaClinicaService) {
        this.historiaClinicaService = historiaClinicaService;
    }

    /**
     * Obtiene el listado completo de consultas en formato JSON.
     */
    @GetMapping
    public ResponseEntity<List<HistoriaClinicaDTO>> listarTodas() {
        List<HistoriaClinicaDTO> dtos = historiaClinicaService.listarConsultas().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene los detalles de una consulta clínica en formato JSON.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HistoriaClinicaDTO> obtenerPorId(@PathVariable Long id) {
        return historiaClinicaService.obtenerDetalleConsulta(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene el historial clínico completo de una mascota en formato JSON.
     */
    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<HistoriaClinicaDTO>> obtenerPorMascota(@PathVariable Long mascotaId) {
        List<HistoriaClinicaDTO> dtos = historiaClinicaService.buscarPorMascota(mascotaId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Convierte una entidad de HistoriaClinica en HistoriaClinicaDTO para su serialización segura.
     */
    private HistoriaClinicaDTO convertirADTO(HistoriaClinica h) {
        return new HistoriaClinicaDTO(
                h.getId(),
                h.getCodigoConsulta(),
                h.getFechaConsulta(),
                h.getMotivoConsulta(),
                h.getAnamnesis(),
                h.getTemperatura(),
                h.getPesoActual(),
                h.getFrecuenciaCardiaca(),
                h.getFrecuenciaRespiratoria(),
                h.getDiagnostico(),
                h.getTratamiento(),
                h.getMedicamentos(),
                h.getRecomendaciones(),
                h.getProximaVisita(),
                h.getEstadoConsulta(),
                h.getVeterinario(),
                h.getMascota().getId()
        );
    }
}
